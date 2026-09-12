package com.dansplugins.currencies.trace

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

/**
 * Wraps a command's executor so each use is reported to trace before it is
 * handled. Only the command's name is reported -- never the sender, the label
 * or the arguments. Tab completion is passed straight through, unreported.
 */
class TraceReportingCommand<T>(
    private val trace: TraceClient,
    private val delegate: T
) : CommandExecutor, TabCompleter where T : CommandExecutor, T : TabCompleter {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        trace.report("command", null, mapOf("name" to command.name))
        return delegate.onCommand(sender, command, label, args)
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String>? = delegate.onTabComplete(sender, command, alias, args)
}
