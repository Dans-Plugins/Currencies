package com.dansplugins.currencies.command.currency.set.name

import com.dansplugins.currencies.Currencies
import com.dansplugins.currencies.currency.Currency
import com.dansplugins.currencies.currency.CurrencyId
import com.dansplugins.factionsystem.player.MfPlayerId
import dev.forkhandles.result4k.onFailure
import org.bukkit.ChatColor.GREEN
import org.bukkit.ChatColor.RED
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.conversations.ConversationContext
import org.bukkit.conversations.ConversationFactory
import org.bukkit.conversations.Prompt
import org.bukkit.conversations.StringPrompt
import org.bukkit.entity.Player
import preponderous.ponder.command.unquote
import java.util.logging.Level

class CurrencySetNameCommand(private val plugin: Currencies) : CommandExecutor, TabCompleter {
    private val conversationFactory = ConversationFactory(plugin)
        .withModality(true)
        .withFirstPrompt(NamePrompt())
        .withEscapeSequence("cancel")
        .withLocalEcho(false)
        .thatExcludesNonPlayersWithMessage("${RED}You must be a player to rename currencies.")
        .addConversationAbandonedListener { event ->
            if (!event.gracefulExit()) {
                val conversable = event.context.forWhom
                if (conversable is Player) {
                    conversable.sendMessage("${RED}Operation cancelled.")
                }
            }
        }

    private inner class NamePrompt : StringPrompt() {
        override fun getPromptText(context: ConversationContext): String = "Enter the new name of the currency, or type 'cancel' to cancel."
        override fun acceptInput(context: ConversationContext, input: String?): Prompt? {
            val conversable = context.forWhom
            if (conversable !is Player) return END_OF_CONVERSATION
            if (input == null) return END_OF_CONVERSATION
            setCurrencyName(conversable, context.getSessionData("currency") as String, input)
            return END_OF_CONVERSATION
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("currencies.rename")) {
            sender.sendMessage("${RED}You do not have permission to rename currencies.")
            return true
        }
        if (sender !is Player) {
            sender.sendMessage("${RED}You must be a player to rename currencies.")
            return true
        }
        val unquotedArgs = args.unquote()
        if (unquotedArgs.isEmpty()) {
            sender.sendMessage("${RED}Usage: /currency set name [currency name] (new currency name)")
            return true
        }
        if (unquotedArgs.size < 2) {
            val conversation = conversationFactory.buildConversation(sender)
            conversation.context.setSessionData("currency", unquotedArgs[0])
            conversation.begin()
            return true
        }
        setCurrencyName(sender, unquotedArgs[0], unquotedArgs.drop(1).joinToString(" "))
        return true
    }

    private fun setCurrencyName(player: Player, oldName: String, newName: String) {
        val hasForcePermission = player.hasPermission("currencies.force.rename")
        plugin.server.scheduler.runTaskAsynchronously(plugin, Runnable {
            val playerId = MfPlayerId.fromBukkitPlayer(player)
            val factionService = plugin.medievalFactions.services.factionService
            val playerFaction = factionService.getFaction(playerId)
            val currencyService = plugin.services.currencyService
            val currency = currencyService.getCurrency(CurrencyId(oldName)) ?: currencyService.getCurrency(oldName)
            if (currency == null) {
                player.sendMessage("${RED}There is no currency by that name.")
                return@Runnable
            }
            val existingCurrency = currencyService.getCurrency(newName)
            if (existingCurrency != null) {
                player.sendMessage("${RED}There is already a currency with that name.")
                return@Runnable
            }
            val role = playerFaction?.getRole(playerId)
            if (!hasForcePermission && (playerFaction?.id != currency.factionId || role == null || !role.hasPermission(playerFaction, plugin.factionPermissions.changeCurrencyName(currency.id)))) {
                player.sendMessage("${RED}Your role in this faction does not give you permission to change the name of this currency.")
                return@Runnable
            }
            val updatedCurrency = currencyService.save(currency.copy(name = newName)).onFailure {
                player.sendMessage("${RED}Failed to save currency.")
                plugin.logger.log(Level.SEVERE, "Failed to save currency: ${it.reason.message}", it.reason.cause)
                return@Runnable
            }
            player.sendMessage("${GREEN}Currency name changed from ${currency.name} to ${updatedCurrency.name}.")
            plugin.server.scheduler.runTask(plugin, Runnable {
                player.performCommand("currency info ${currency.id.value}")
            })
        })
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