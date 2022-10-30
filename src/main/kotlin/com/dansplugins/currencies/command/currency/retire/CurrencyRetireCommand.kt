package com.dansplugins.currencies.command.currency.retire

import com.dansplugins.currencies.Currencies
import com.dansplugins.currencies.currency.Currency
import com.dansplugins.currencies.currency.CurrencyId
import com.dansplugins.currencies.currency.CurrencyStatus.ACTIVE
import com.dansplugins.currencies.currency.CurrencyStatus.RETIRED
import com.dansplugins.factionsystem.player.MfPlayerId
import dev.forkhandles.result4k.onFailure
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
import org.bukkit.entity.Player
import java.util.logging.Level.SEVERE
import net.md_5.bungee.api.ChatColor as SpigotChatColor
import org.bukkit.ChatColor as BukkitChatColor

class CurrencyRetireCommand(private val plugin: Currencies) : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("currencies.retire")) {
            sender.sendMessage("${BukkitChatColor.RED}You do not have permission to retire currencies.")
            return true
        }
        if (sender !is Player) {
            sender.sendMessage("${BukkitChatColor.RED}You must be a player to retire currencies.")
            return true
        }
        if (args.isEmpty()) {
            sender.sendMessage("${BukkitChatColor.RED}Usage: /currency retire [currency]")
            return true
        }
        plugin.server.scheduler.runTaskAsynchronously(plugin, Runnable {
            val isConfirm = args.size > 1 && args.last().equals("confirm", ignoreCase = true)
            val currencyService = plugin.services.currencyService
            val currency = currencyService.getCurrency(CurrencyId(args[0]))
                ?: currencyService.getCurrency(args.dropLast(if (isConfirm) 1 else 0).joinToString(" "))
            if (currency == null) {
                sender.sendMessage("${BukkitChatColor.RED}There is no currency by that name.")
                return@Runnable
            }
            if (currency.status != ACTIVE) {
                sender.sendMessage("${BukkitChatColor.RED}That currency is no longer active.")
                return@Runnable
            }
            val playerId = MfPlayerId.fromBukkitPlayer(sender)
            val factionService = plugin.medievalFactions.services.factionService
            val playerFaction = factionService.getFaction(playerId)
            val role = playerFaction?.getRole(playerId)
            if (playerFaction?.id != currency.factionId || role == null || !role.hasPermission(
                    playerFaction,
                    plugin.factionPermissions.retireCurrency(currency.id)
                )
            ) {
                sender.sendMessage("${BukkitChatColor.RED}Your role in this faction does not give you permission to retire this currency.")
                return@Runnable
            }
            if (isConfirm) {
                currencyService.save(currency.copy(status = RETIRED)).onFailure {
                    sender.sendMessage("${BukkitChatColor.RED}Failed to save currency.")
                    plugin.logger.log(SEVERE, "Failed to save currency: ${it.reason.message}", it.reason.cause)
                    return@Runnable
                }
                sender.sendMessage("${BukkitChatColor.GREEN}Currency retired.")
                playerFaction.sendMessage("Currency retired", "The currency ${currency.name} was retired, and will no longer be minted.")
            } else {
                sender.sendMessage("${BukkitChatColor.GRAY}Are you sure you wish to retire ${currency.name}? This action is irreversible and will prevent it being minted.")
                sender.spigot().sendMessage(
                    TextComponent("Confirm").apply {
                        color = SpigotChatColor.GREEN
                        hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to retire ${currency.name}."))
                        clickEvent = ClickEvent(RUN_COMMAND, "/currency retire ${currency.id.value} confirm")
                    }
                )
            }
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