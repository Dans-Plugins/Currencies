package com.dansplugins.currencies.command.currency.set

import com.dansplugins.currencies.Currencies
import com.dansplugins.currencies.command.currency.set.description.CurrencySetDescriptionCommand
import com.dansplugins.currencies.command.currency.set.name.CurrencySetNameCommand
import org.bukkit.ChatColor.RED
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class CurrencySetCommand(private val plugin: Currencies) : CommandExecutor, TabCompleter {
    private val setNameCommand = CurrencySetNameCommand(plugin)
    private val setDescriptionCommand = CurrencySetDescriptionCommand(plugin)

    private val subcommands = listOf("name", "description", "desc")

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        return when (args.firstOrNull()?.lowercase()) {
            "name" -> setNameCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            "description", "desc" -> setDescriptionCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            else -> {
                sender.sendMessage("${RED}Usage: /currency set [name|desc]")
                true
            }
        }
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ) = when {
        args.isEmpty() -> subcommands
        args.size == 1 -> subcommands.filter { it.startsWith(args[0].lowercase()) }
        else -> when (args[0].lowercase()) {
            "name" -> setNameCommand.onTabComplete(sender, command, alias, args.drop(1).toTypedArray())
            "description", "desc" -> setDescriptionCommand.onTabComplete(sender, command, alias, args.drop(1).toTypedArray())
            else -> emptyList()
        }
    }
}