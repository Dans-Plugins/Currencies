package com.dansplugins.currencies.command.currency.info

import com.dansplugins.currencies.Currencies
import com.dansplugins.currencies.currency.Currency
import com.dansplugins.currencies.currency.CurrencyId
import com.dansplugins.currencies.item.tagToNbtJson
import com.dansplugins.factionsystem.player.MfPlayerId
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_ITEM
import net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT
import net.md_5.bungee.api.chat.ItemTag
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Item
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import net.md_5.bungee.api.ChatColor as SpigotChatColor
import org.bukkit.ChatColor as BukkitChatColor

class CurrencyInfoCommand(private val plugin: Currencies) : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            sender.sendMessage("${BukkitChatColor.RED}Usage: /currency info [currency]")
            return true
        }
        val showAmountMinted = plugin.config.getBoolean("currencies.showAmountMinted")
        plugin.server.scheduler.runTaskAsynchronously(plugin, Runnable {
            val playerId = if (sender is Player) MfPlayerId.fromBukkitPlayer(sender) else null
            val factionService = plugin.medievalFactions.services.factionService
            val playerFaction = playerId?.let { factionService.getFaction(it) }
            val playerRole = playerFaction?.getRole(playerId)
            val currencyService = plugin.services.currencyService
            val currency = currencyService.getCurrency(CurrencyId(args[0])) ?: currencyService.getCurrency(args.joinToString(" "))
            if (currency == null) {
                sender.sendMessage("${BukkitChatColor.RED}There is no currency by that name.")
                return@Runnable
            }
            sender.sendMessage("${BukkitChatColor.AQUA}=== ${currency.name} ===")
            if (playerFaction?.id == currency.factionId
                && playerRole != null
                && playerRole.hasPermission(playerFaction, plugin.factionPermissions.changeCurrencyName(currency.id))) {
                sender.spigot().sendMessage(
                    TextComponent("Change name").apply {
                        color = SpigotChatColor.GREEN
                        hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to change this currency's name"))
                        clickEvent = ClickEvent(RUN_COMMAND, "/currency set name ${currency.id.value}")
                    }
                )
            }
            sender.sendMessage("${BukkitChatColor.GRAY}Description: ${currency.description}")
            if (playerFaction?.id == currency.factionId
                && playerRole != null
                && playerRole.hasPermission(playerFaction, plugin.factionPermissions.changeCurrencyDescription(currency.id))) {
                sender.spigot().sendMessage(
                    TextComponent("Change description").apply {
                        color = SpigotChatColor.GREEN
                        hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to change this currency's description"))
                        clickEvent = ClickEvent(RUN_COMMAND, "/currency set description ${currency.id.value}")
                    }
                )
            }
            val currencyFaction = factionService.getFaction(currency.factionId)
            if (currencyFaction != null) {
                sender.sendMessage("${BukkitChatColor.GRAY}Faction: ${currencyFaction.name}")
            }
            val itemMeta = if (currency.item.hasItemMeta()) currency.item.itemMeta else null
            val displayName = if (itemMeta?.hasDisplayName() == true) itemMeta.displayName else null
            val itemName = displayName ?: currency.item.type.name.lowercase().replace('_', ' ')
            val tagNbtJson = currency.item.tagToNbtJson()
            if (tagNbtJson != null) {
                sender.spigot().sendMessage(
                    TextComponent("Item: ").apply {
                        color = SpigotChatColor.GRAY
                    },
                    TextComponent(itemName).apply {
                        color = SpigotChatColor.GRAY
                        hoverEvent =
                            HoverEvent(SHOW_ITEM, Item(currency.item.type.key.toString(), 1, ItemTag.ofNbt(tagNbtJson)))
                    }
                )
            } else {
                sender.sendMessage("${BukkitChatColor.GRAY}Item: $itemName")
            }
            if (showAmountMinted) {
                sender.sendMessage("${BukkitChatColor.GRAY}Minted: ${currency.amount}")
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