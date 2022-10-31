package com.dansplugins.currencies.command.currency

import com.dansplugins.currencies.Currencies
import com.dansplugins.currencies.command.currency.balance.CurrencyBalanceCommand
import com.dansplugins.currencies.command.currency.create.CurrencyCreateCommand
import com.dansplugins.currencies.command.currency.info.CurrencyInfoCommand
import com.dansplugins.currencies.command.currency.list.CurrencyListCommand
import com.dansplugins.currencies.command.currency.mint.CurrencyMintCommand
import com.dansplugins.currencies.command.currency.retire.CurrencyRetireCommand
import com.dansplugins.currencies.command.currency.set.CurrencySetCommand
import com.dansplugins.currencies.command.currency.set.name.CurrencySetNameCommand
import org.bukkit.ChatColor.RED
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class CurrencyCommand(private val plugin: Currencies) : CommandExecutor, TabCompleter {
    private val balanceCommand = CurrencyBalanceCommand(plugin)
    private val createCommand = CurrencyCreateCommand(plugin)
    private val infoCommand = CurrencyInfoCommand(plugin)
    private val setCommand = CurrencySetCommand(plugin)
    private val setNameCommand = CurrencySetNameCommand(plugin)
    private val listCommand = CurrencyListCommand(plugin)
    private val mintCommand = CurrencyMintCommand(plugin)
    private val retireCommand = CurrencyRetireCommand(plugin)

    private val subcommands = listOf(
        "balance",
        "create",
        "info",
        "set",
        "rename",
        "list",
        "mint",
        "retire"
    )

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        return when (args.firstOrNull()?.lowercase()) {
            "balance" -> balanceCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            "create" -> createCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            "info" -> infoCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            "set" -> setCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            "rename" -> setNameCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            "list" -> listCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            "mint" -> mintCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            "retire" -> retireCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            else -> {
                sender.sendMessage("${RED}Usage: /currency [balance|create]")
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
        else -> when (args.first().lowercase()) {
            "balance" -> balanceCommand.onTabComplete(sender, command, alias, args.drop(1).toTypedArray())
            "create" -> createCommand.onTabComplete(sender, command, alias, args.drop(1).toTypedArray())
            "info" -> infoCommand.onTabComplete(sender, command, alias, args.drop(1).toTypedArray())
            "set" -> setCommand.onTabComplete(sender, command, alias, args.drop(1).toTypedArray())
            "rename" -> setNameCommand.onTabComplete(sender, command, alias, args.drop(1).toTypedArray())
            "list" -> listCommand.onTabComplete(sender, command, alias, args.drop(1).toTypedArray())
            "mint" -> mintCommand.onTabComplete(sender, command, alias, args.drop(1).toTypedArray())
            "retire" -> retireCommand.onTabComplete(sender, command, alias, args.drop(1).toTypedArray())
            else -> emptyList()
        }
    }
}