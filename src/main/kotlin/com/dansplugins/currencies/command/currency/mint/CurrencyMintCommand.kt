package com.dansplugins.currencies.command.currency.mint

import com.dansplugins.currencies.Currencies
import com.dansplugins.currencies.command.unquoteSafely
import com.dansplugins.currencies.currency.Currency
import com.dansplugins.currencies.currency.CurrencyId
import com.dansplugins.currencies.currency.CurrencyStatus.ACTIVE
import com.dansplugins.factionsystem.player.MfPlayer
import com.dansplugins.factionsystem.player.MfPlayerId
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.onFailure
import org.bukkit.ChatColor.GREEN
import org.bukkit.ChatColor.RED
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.logging.Level.SEVERE

class CurrencyMintCommand(private val plugin: Currencies) : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("currencies.mint")) {
            sender.sendMessage("${RED}You do not have permission to mint currencies.")
            return true
        }
        if (sender !is Player) {
            sender.sendMessage("${RED}You must be a player to mint currencies.")
            return true
        }
        if (args.isEmpty()) {
            sender.sendMessage("${RED}Usage: /currency mint [currency] (amount)")
            return true
        }
        val unquotedArgs = args.unquoteSafely()
        val powerCostEnabled = plugin.config.getBoolean("currencies.powerCostEnabled")
        val powerCost = plugin.config.getDouble("currencies.powerCost")
        val itemCostEnabled = plugin.config.getBoolean("currencies.itemCostEnabled")
        plugin.server.scheduler.runTaskAsynchronously(plugin, Runnable {
            val parsedAmount = if (unquotedArgs.size > 1) unquotedArgs.last().toIntOrNull() else null
            val lastArgOffset = if (parsedAmount == null) 0 else 1
            val amount = parsedAmount ?: 1
            // A zero or negative amount would credit power back and lower the minted total (#225).
            if (amount < 1) {
                sender.sendMessage("${RED}The amount must be at least 1.")
                return@Runnable
            }
            val currencyService = plugin.services.currencyService
            val currency = currencyService.getCurrency(CurrencyId(unquotedArgs[0]))
                ?: currencyService.getCurrency(unquotedArgs.dropLast(lastArgOffset).joinToString(" "))
            if (currency == null) {
                sender.sendMessage("${RED}There is no currency by that name.")
                return@Runnable
            }
            if (currency.status != ACTIVE) {
                sender.sendMessage("${RED}That currency is no longer active.")
                return@Runnable
            }
            val playerId = MfPlayerId.fromBukkitPlayer(sender)
            val playerService = plugin.medievalFactions.services.playerService
            val mfPlayer = playerService.getPlayer(playerId) ?: playerService.save(MfPlayer(plugin.medievalFactions, sender)).onFailure {
                sender.sendMessage("${RED}Failed to save player information.")
                plugin.logger.log(SEVERE, "Failed to save player: ${it.reason.message}", it.reason.cause)
                return@Runnable
            }
            val factionService = plugin.medievalFactions.services.factionService
            val playerFaction = factionService.getFaction(playerId)
            val role = playerFaction?.getRole(playerId)
            if (playerFaction?.id != currency.factionId || role == null || !role.hasPermission(playerFaction, plugin.factionPermissions.mintCurrency(currency.id))) {
                sender.sendMessage("${RED}Your role in this faction does not give you permission to mint this currency.")
                return@Runnable
            }
            if (powerCostEnabled) {
                val powerRequired = amount * powerCost
                if (mfPlayer.power < powerRequired) {
                    sender.sendMessage("${RED}You need at least $powerRequired power to mint $amount ${currency.name}")
                    return@Runnable
                }
            }
            plugin.server.scheduler.runTask(plugin, Runnable syncTask@{
                if (itemCostEnabled) {
                    if (!sender.inventory.containsAtLeast(ItemStack(currency.item.type), amount)) {
                        sender.sendMessage("${RED}You need at least $amount x ${currency.item.type.toString().lowercase().replace('_', ' ')} to mint $amount ${currency.name}")
                        return@syncTask
                    }
                    // Taken before anything is saved so that the items cannot be spent elsewhere
                    // while the saves run. They are handed back if either save fails (#296).
                    sender.inventory.removeItem(ItemStack(currency.item.type, amount))
                }
                plugin.server.scheduler.runTaskAsynchronously(plugin, Runnable {
                    val recorded = recordMint(sender, mfPlayer, currency, amount, powerCostEnabled, powerCost)
                    plugin.server.scheduler.runTask(plugin, Runnable {
                        // Coins are only given once the cost and the minted total have both been saved (#296).
                        if (recorded) {
                            sender.giveOrDrop(ItemStack(currency.item).apply { this.amount = amount })
                            sender.sendMessage("${GREEN}Minted $amount x ${currency.name}.")
                        } else if (itemCostEnabled) {
                            sender.giveOrDrop(ItemStack(currency.item.type, amount))
                        }
                    })
                })
            })
        })
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ) = when {
        args.isEmpty() -> plugin.services.currencyService.currencies.map(Currency::name)
        args.size == 1 -> plugin.services.currencyService.currencies.map(Currency::name).filter { it.lowercase().startsWith(args[0].lowercase()) }
        else -> emptyList()
    }

    /**
     * Saves the power cost and then the minted total, refunding the power when the minted total
     * cannot be saved. Returns whether both were saved; when not, the sender has been told why.
     */
    private fun recordMint(
        sender: Player,
        snapshot: MfPlayer,
        currency: Currency,
        amount: Int,
        powerCostEnabled: Boolean,
        powerCost: Double
    ): Boolean {
        val powerRequired = amount * powerCost
        if (powerCostEnabled) {
            val playerService = plugin.medievalFactions.services.playerService
            // Re-read so that the cost is taken from the player's current power rather than the snapshot checked earlier.
            val current = playerService.getPlayer(snapshot.id) ?: snapshot
            if (current.power < powerRequired) {
                sender.sendMessage("${RED}You need at least $powerRequired power to mint $amount ${currency.name}")
                return false
            }
            when (val result = playerService.save(current.copy(power = current.power - powerRequired))) {
                is Success -> Unit
                is Failure -> {
                    sender.sendMessage("${RED}Failed to save player information.")
                    plugin.logger.log(SEVERE, "Failed to save player: ${result.reason.message}", result.reason.cause)
                    return false
                }
            }
        }
        if (saveMintedTotal(sender, currency.id, amount)) return true
        if (powerCostEnabled) refundPower(snapshot, powerRequired)
        return false
    }

    /**
     * Adds [amount] to the currency's minted total. The currency is re-read under a lock so that
     * mints run close together each build on the version the previous one saved, rather than on a
     * shared snapshot that the optimistic-locking check would refuse for all but the first (#296).
     */
    private fun saveMintedTotal(sender: Player, currencyId: CurrencyId, amount: Int): Boolean = synchronized(mintLock) {
        val currencyService = plugin.services.currencyService
        val current = currencyService.getCurrency(currencyId)
        if (current == null || current.status != ACTIVE) {
            sender.sendMessage("${RED}That currency is no longer active.")
            return@synchronized false
        }
        when (val result = currencyService.save(current.copy(amount = current.amount + amount))) {
            is Success -> true
            is Failure -> {
                sender.sendMessage("${RED}Failed to save currency.")
                plugin.logger.log(SEVERE, "Failed to save currency: ${result.reason.message}", result.reason.cause)
                false
            }
        }
    }

    private fun refundPower(snapshot: MfPlayer, powerRequired: Double) {
        val playerService = plugin.medievalFactions.services.playerService
        val current = playerService.getPlayer(snapshot.id) ?: snapshot
        val result = playerService.save(current.copy(power = current.power + powerRequired))
        if (result is Failure) {
            plugin.logger.log(SEVERE, "Failed to refund $powerRequired power to ${snapshot.name} after a failed mint: ${result.reason.message}", result.reason.cause)
        }
    }

    private fun Player.giveOrDrop(item: ItemStack) {
        inventory.addItem(item).values.forEach { world.dropItem(location, it) }
    }

    private companion object {
        val mintLock = Any()
    }
}
