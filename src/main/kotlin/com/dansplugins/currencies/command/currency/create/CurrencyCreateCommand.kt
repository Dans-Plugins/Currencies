package com.dansplugins.currencies.command.currency.create

import com.dansplugins.currencies.Currencies
import com.dansplugins.currencies.currency.Currency
import com.dansplugins.currencies.currency.CurrencyId
import com.dansplugins.factionsystem.player.MfPlayerId
import dev.forkhandles.result4k.onFailure
import org.bukkit.ChatColor.GREEN
import org.bukkit.ChatColor.RED
import org.bukkit.NamespacedKey
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType.STRING
import java.util.logging.Level.SEVERE

class CurrencyCreateCommand(private val plugin: Currencies) : CommandExecutor, TabCompleter {
    private val currencyIdKey = NamespacedKey(plugin, "currencyId")

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("currencies.create")) {
            sender.sendMessage("${RED}You do not have permission to create currencies.")
            return true
        }
        if (sender !is Player) {
            sender.sendMessage("${RED}You must be a player to perform this command.")
            return true
        }
        if (args.isEmpty()) {
            sender.sendMessage("${RED}Usage: /currency create [currency name]")
            return true
        }
        val itemInHand = sender.inventory.itemInMainHand
        if (itemInHand.amount == 0 || itemInHand.type.isAir) {
            sender.sendMessage("${RED}You must hold the item you wish to use for this currency.")
            return true
        }
        val currencyId = CurrencyId.generate()
        val item = ItemStack(itemInHand)
        item.amount = 1
        item.itemMeta = (item.itemMeta ?: plugin.server.itemFactory.getItemMeta(item.type))?.apply {
            persistentDataContainer.set(currencyIdKey, STRING, currencyId.value)
            setDisplayName(args.joinToString(" "))
        }
        val medievalFactions = plugin.medievalFactions
        plugin.server.scheduler.runTaskAsynchronously(plugin, Runnable {
            val factionService = medievalFactions.services.factionService
            val playerId = MfPlayerId.fromBukkitPlayer(sender)
            val faction = factionService.getFaction(playerId)
            if (faction == null) {
                sender.sendMessage("${RED}You must be in a faction to create a currency.")
                return@Runnable
            }
            val role = faction.getRole(playerId)
            if (role == null || !role.hasPermission(faction, plugin.factionPermissions.createCurrency)) {
                sender.sendMessage("${RED}Your role in this faction does not give you permission to create currencies.")
                return@Runnable
            }
            val currencyService = plugin.services.currencyService
            val existingCurrency = currencyService.getCurrency(args.joinToString(" "))
            if (existingCurrency != null) {
                sender.sendMessage("${RED}There is already a currency with that name.")
                return@Runnable
            }
            val currency = currencyService.save(Currency(
                id = currencyId,
                name = args.joinToString(" "),
                factionId = faction.id,
                item = item
            )).onFailure {
                sender.sendMessage("${RED}Failed to save currency.")
                plugin.logger.log(SEVERE, "Failed to save currency: ${it.reason.message}", it.reason.cause)
                return@Runnable
            }
            factionService.save(faction.copy(
                defaultPermissionsByName = faction.defaultPermissionsByName + mapOf(
                    plugin.factionPermissions.retireCurrency(currency.id).name to false,
                    plugin.factionPermissions.changeCurrencyName(currency.id).name to false,
                    plugin.factionPermissions.changeCurrencyDescription(currency.id).name to false,
                    plugin.factionPermissions.mintCurrency(currency.id).name to false,
                    medievalFactions.factionPermissions.setRolePermission(plugin.factionPermissions.retireCurrency(currency.id)).name to false,
                    medievalFactions.factionPermissions.setRolePermission(plugin.factionPermissions.changeCurrencyName(currency.id)).name to false,
                    medievalFactions.factionPermissions.setRolePermission(plugin.factionPermissions.changeCurrencyDescription(currency.id)).name to false,
                    medievalFactions.factionPermissions.setRolePermission(plugin.factionPermissions.mintCurrency(currency.id)).name to false
                ),
                roles = faction.roles.copy(roles = faction.roles.map {
                    if (it.hasPermission(faction, plugin.factionPermissions.createCurrency)) {
                        it.copy(permissionsByName = it.permissionsByName + mapOf(
                            plugin.factionPermissions.retireCurrency(currency.id).name to true,
                            plugin.factionPermissions.changeCurrencyName(currency.id).name to true,
                            plugin.factionPermissions.changeCurrencyDescription(currency.id).name to true,
                            plugin.factionPermissions.mintCurrency(currency.id).name to true,
                            medievalFactions.factionPermissions.setRolePermission(plugin.factionPermissions.retireCurrency(currency.id)).name to true,
                            medievalFactions.factionPermissions.setRolePermission(plugin.factionPermissions.changeCurrencyName(currency.id)).name to true,
                            medievalFactions.factionPermissions.setRolePermission(plugin.factionPermissions.changeCurrencyDescription(currency.id)).name to true,
                            medievalFactions.factionPermissions.setRolePermission(plugin.factionPermissions.mintCurrency(currency.id)).name to true
                        ))
                    } else {
                        it
                    }
                })
            )).onFailure {
                sender.sendMessage("${RED}Failed to save faction.")
                plugin.logger.log(SEVERE, "Failed to save faction: ${it.reason.message}", it.reason.cause)
                return@Runnable
            }
            sender.sendMessage("${GREEN}Currency created.")
        })
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ) = emptyList<String>()
}