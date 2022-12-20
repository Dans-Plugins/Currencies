package com.dansplugins.currencies.command.currency.mint

import com.dansplugins.currencies.Currencies
import com.dansplugins.currencies.currency.Currency
import com.dansplugins.currencies.currency.CurrencyId
import com.dansplugins.currencies.currency.CurrencyStatus.ACTIVE
import com.dansplugins.factionsystem.player.MfPlayer
import com.dansplugins.factionsystem.player.MfPlayerId
import dev.forkhandles.result4k.onFailure
import org.bukkit.ChatColor.GREEN
import org.bukkit.ChatColor.RED
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import preponderous.ponder.command.unquote
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
        val unquotedArgs = args.unquote()
        val powerCostEnabled = plugin.config.getBoolean("currencies.powerCostEnabled")
        val powerCost = plugin.config.getDouble("currencies.powerCost")
        val itemCostEnabled = plugin.config.getBoolean("currencies.itemCostEnabled")
        plugin.server.scheduler.runTaskAsynchronously(plugin, Runnable {
            val parsedAmount = if (unquotedArgs.size > 1) unquotedArgs.last().toIntOrNull() else null
            val lastArgOffset = if (parsedAmount == null) 0 else 1
            val amount = parsedAmount ?: 1
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
                }
                val item = ItemStack(currency.item).apply {
                    this.amount = amount
                }
                sender.inventory.addItem(item).values.forEach { sender.world.dropItem(sender.location, it) }
                if (itemCostEnabled) {
                    sender.inventory.removeItem(ItemStack(currency.item.type, amount))
                }
                plugin.server.scheduler.runTaskAsynchronously(plugin, Runnable asyncTask@{
                    if (powerCostEnabled) {
                        playerService.save(mfPlayer.copy(power = mfPlayer.power - (amount * powerCost))).onFailure {
                            sender.sendMessage("${RED}Failed to save player information.")
                            plugin.logger.log(SEVERE, "Failed to save player: ${it.reason.message}", it.reason.cause)
                            return@asyncTask
                        }
                    }
                    currencyService.save(currency.copy(amount = currency.amount + amount)).onFailure {
                        sender.sendMessage("${RED}Failed to save currency.")
                        plugin.logger.log(SEVERE, "Failed to save currency: ${it.reason.message}", it.reason.cause)
                        return@asyncTask
                    }
                    sender.sendMessage("${GREEN}Minted $amount x ${currency.name}.")
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
}