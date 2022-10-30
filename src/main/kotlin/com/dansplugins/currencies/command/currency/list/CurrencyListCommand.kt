package com.dansplugins.currencies.command.currency.list

import com.dansplugins.currencies.Currencies
import com.dansplugins.currencies.currency.CurrencyStatus.ACTIVE
import com.dansplugins.currencies.currency.CurrencyStatus.RETIRED
import com.dansplugins.factionsystem.faction.MfFaction
import com.dansplugins.factionsystem.faction.MfFactionId
import com.dansplugins.factionsystem.pagination.PaginatedView
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import net.md_5.bungee.api.ChatColor as SpigotChatColor
import org.bukkit.ChatColor as BukkitChatColor

class CurrencyListCommand(private val plugin: Currencies) : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("currencies.list")) {
            sender.sendMessage("${BukkitChatColor.RED}You do not have permission to view the currency list.")
            return true
        }
        plugin.server.scheduler.runTaskAsynchronously(plugin, Runnable {
            val lastArgOffset = if (args.lastOrNull()?.toIntOrNull() != null) 1 else 0
            val factionService = plugin.medievalFactions.services.factionService
            val faction = if (args.isNotEmpty()) {
                factionService.getFaction(MfFactionId(args[0])) ?: factionService.getFaction(args.dropLast(lastArgOffset).joinToString(" "))
            } else {
                null
            }
            val currencyService = plugin.services.currencyService
            val filter = when {
                args.isEmpty() || args.firstOrNull()?.toIntOrNull() != null -> ""
                args[0].equals("active", ignoreCase = true) -> "active "
                args[0].equals("retired", ignoreCase = true) -> "retired "
                faction != null -> "${faction.id.value} "
                else -> ""
            }
            val currencies = when {
                args.isEmpty() || args.firstOrNull()?.toIntOrNull() != null -> currencyService.currencies
                args[0].equals("active", ignoreCase = true) -> currencyService.getCurrencies(ACTIVE)
                args[0].equals("retired", ignoreCase = true) -> currencyService.getCurrencies(RETIRED)
                faction != null -> currencyService.getCurrencies(faction.id)
                else -> null
            }
            if (currencies == null) {
                sender.sendMessage("${BukkitChatColor.RED}Invalid filter, you must specify 'active', 'retired' or a faction name.")
                return@Runnable
            }
            val pageNumber = args.lastOrNull()?.toIntOrNull()?.minus(1) ?: 0
            val view = PaginatedView(
                plugin.medievalFactions.language,
                lazy {
                    arrayOf(
                        TextComponent("=== Currencies ===").apply {
                            color = SpigotChatColor.AQUA
                            isBold = true
                        }
                    )
                },
                currencies
                    .map { currency ->
                        lazy {
                            arrayOf(
                                TextComponent(currency.name).apply {
                                    color = SpigotChatColor.GRAY
                                    hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to view information on ${currency.name}"))
                                    clickEvent = ClickEvent(RUN_COMMAND, "/currency info ${currency.id.value}")
                                }
                            )
                        }
                    }
            ) { page ->

                "/currency list $filter${page + 1}"
            }
            if (view.pages.isEmpty()) {
                sender.sendMessage("${BukkitChatColor.RED}There are currently no currencies.")
                return@Runnable
            }
            if (pageNumber !in view.pages.indices) {
                sender.sendMessage("${BukkitChatColor.RED}Invalid page number.")
                return@Runnable
            }
            view.sendPage(sender, pageNumber)
        })
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ) = when {
        args.isEmpty() -> listOf("active", "retired") + plugin.medievalFactions.services.factionService.factions.map(MfFaction::name)
        args.size == 1 -> (listOf("active", "retired") + plugin.medievalFactions.services.factionService.factions.map(MfFaction::name)).filter { it.lowercase().startsWith(args[0].lowercase()) }
        else -> emptyList()
    }
}