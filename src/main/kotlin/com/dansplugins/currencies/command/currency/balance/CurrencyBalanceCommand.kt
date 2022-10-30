package com.dansplugins.currencies.command.currency.balance

import com.dansplugins.currencies.Currencies
import com.dansplugins.currencies.currency.CurrencyStatus.RETIRED
import com.dansplugins.factionsystem.player.MfPlayerId
import org.bukkit.ChatColor
import org.bukkit.ChatColor.RED
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class CurrencyBalanceCommand(private val plugin: Currencies) : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("currencies.balance")) {
            sender.sendMessage("${RED}You do not have permission to view your balance.")
            return true
        }
        if (sender !is Player) {
            sender.sendMessage("${RED}You must be a player to use this command.")
            return true
        }
        plugin.server.scheduler.runTaskAsynchronously(plugin, Runnable {
            val playerId = MfPlayerId.fromBukkitPlayer(sender)
            val currencyService = plugin.services.currencyService
            val balanceService = plugin.services.balanceService
            val currencies = currencyService.currencies
            val balances = balanceService.getBalances(playerId).associateBy { it.currencyId }
            sender.sendMessage("${ChatColor.AQUA}=== Coinpurse Contents ===")
            currencies.forEach { currency ->
                val balance = balances[currency.id]?.balance ?: 0
                sender.sendMessage("${ChatColor.GRAY}${currency.name}: $balance${if (currency.status == RETIRED) " ${RED}[retired]" else ""}")
            }
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