package com.dansplugins.currencies.command.coinpurse

import com.dansplugins.currencies.Currencies
import com.dansplugins.currencies.coinpurse.CoinpurseInventoryHolder
import com.dansplugins.factionsystem.player.MfPlayerId
import org.bukkit.ChatColor.RED
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class CoinpurseCommand(private val plugin: Currencies) : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("currencies.coinpurse")) {
            sender.sendMessage("${RED}You do not have permission to open your coinpurse.")
            return true
        }
        if (sender !is Player) {
            sender.sendMessage("${RED}You must be a player to open your coinpurse.")
            return true
        }
        val currencyService = plugin.services.currencyService
        val balanceService = plugin.services.balanceService
        plugin.server.scheduler.runTaskAsynchronously(plugin, Runnable {
            val balances = balanceService.getBalances(MfPlayerId.fromBukkitPlayer(sender)).mapNotNull { balance ->
                currencyService.getCurrency(balance.currencyId)?.let { currency -> currency to balance.balance }
            }
            plugin.server.scheduler.runTask(plugin, Runnable {
                val coinpurseHolder = CoinpurseInventoryHolder(plugin)
                balances.forEach { (currency, balance) ->
                    val currencyItem = currency.item
                    val currencyItemStack = ItemStack(currencyItem).apply {
                        amount = currencyItem.type.maxStackSize
                    }
                    val remainder = balance % currencyItem.type.maxStackSize
                    var i = 0
                    while (i < balance) {
                        val leftOver = coinpurseHolder.inventory.addItem(currencyItemStack)
                        if (leftOver.isNotEmpty()) {
                            leftOver.values.forEach { sender.world.dropItem(sender.location, it) }
                        }
                        i += currencyItem.type.maxStackSize
                    }
                    if (remainder != 0) {
                        val remove = ItemStack(currencyItem).apply {
                            amount = currencyItem.type.maxStackSize - remainder
                        }
                        coinpurseHolder.inventory.removeItem(remove)
                    }
                }
                sender.openInventory(coinpurseHolder.inventory)
            })
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