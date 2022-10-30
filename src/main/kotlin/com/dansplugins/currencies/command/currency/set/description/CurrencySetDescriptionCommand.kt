package com.dansplugins.currencies.command.currency.set.description

import com.dansplugins.currencies.Currencies
import com.dansplugins.currencies.currency.Currency
import com.dansplugins.currencies.currency.CurrencyId
import com.dansplugins.factionsystem.player.MfPlayerId
import dev.forkhandles.result4k.onFailure
import org.bukkit.ChatColor
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

class CurrencySetDescriptionCommand(private val plugin: Currencies) : CommandExecutor, TabCompleter {
    private val conversationFactory = ConversationFactory(plugin)
        .withModality(true)
        .withFirstPrompt(DescriptionPrompt())
        .withEscapeSequence("cancel")
        .withLocalEcho(false)
        .thatExcludesNonPlayersWithMessage("${ChatColor.RED}You must be a player to modify the description of currencies.")
        .addConversationAbandonedListener { event ->
            if (!event.gracefulExit()) {
                val conversable = event.context.forWhom
                if (conversable is Player) {
                    conversable.sendMessage("${ChatColor.RED}Operation cancelled.")
                }
            }
        }

    private fun setOrContinueDescription(context: ConversationContext, input: String?): Prompt? {
        val conversable = context.forWhom
        if (conversable !is Player) return StringPrompt.END_OF_CONVERSATION
        if (input == null) return StringPrompt.END_OF_CONVERSATION
        if (input == "end") {
            setCurrencyDescription(conversable, context.getSessionData("currency") as String, context.getSessionData("description") as? String ?: "")
            return StringPrompt.END_OF_CONVERSATION
        }
        context.setSessionData("description", ((context.getSessionData("description") as? String)?.plus(" ") ?: "") + input)
        return ContinueDescriptionPrompt()
    }

    private inner class DescriptionPrompt : StringPrompt() {
        override fun getPromptText(context: ConversationContext): String = "Start writing the new description, type 'cancel' to cancel, or 'end' to finish entering text."
        override fun acceptInput(context: ConversationContext, input: String?): Prompt? = setOrContinueDescription(context, input)
    }

    private inner class ContinueDescriptionPrompt : StringPrompt() {
        override fun getPromptText(context: ConversationContext): String = "Continue writing the new description, type 'cancel' to cancel, or 'end' to finish entering text."
        override fun acceptInput(context: ConversationContext, input: String?): Prompt? = setOrContinueDescription(context, input)
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("currencies.desc")) {
            sender.sendMessage("${ChatColor.RED}You do not have permission to change the description of currencies.")
            return true
        }
        if (sender !is Player) {
            sender.sendMessage("${ChatColor.RED}You must be a player to change the description of currencies.")
            return true
        }
        val unquotedArgs = args.unquote()
        if (unquotedArgs.isEmpty()) {
            sender.sendMessage("${ChatColor.RED}Usage: /currency set description [currency name] (new description)")
            return true
        }
        if (unquotedArgs.size < 2) {
            val conversation = conversationFactory.buildConversation(sender)
            conversation.context.setSessionData("currency", unquotedArgs[0])
            conversation.begin()
            return true
        }
        setCurrencyDescription(sender, unquotedArgs[0], unquotedArgs.drop(1).joinToString(" "))
        return true
    }

    private fun setCurrencyDescription(player: Player, name: String, description: String) {
        val hasForcePermission = player.hasPermission("currencies.force.desc")
        plugin.server.scheduler.runTaskAsynchronously(plugin, Runnable {
            val playerId = MfPlayerId.fromBukkitPlayer(player)
            val factionService = plugin.medievalFactions.services.factionService
            val playerFaction = factionService.getFaction(playerId)
            val currencyService = plugin.services.currencyService
            val currency = currencyService.getCurrency(CurrencyId(name)) ?: currencyService.getCurrency(name)
            if (currency == null) {
                player.sendMessage("${ChatColor.RED}There is no currency by that name.")
                return@Runnable
            }
            val role = playerFaction?.getRole(playerId)
            if (!hasForcePermission && (playerFaction?.id != currency.factionId || role == null || !role.hasPermission(playerFaction, plugin.factionPermissions.changeCurrencyDescription(currency.id)))) {
                player.sendMessage("${ChatColor.RED}Your role in this faction does not give you permission to change the description of this currency.")
                return@Runnable
            }
            currencyService.save(currency.copy(description = description)).onFailure {
                player.sendMessage("${ChatColor.RED}Failed to save currency.")
                plugin.logger.log(Level.SEVERE, "Failed to save currency: ${it.reason.message}", it.reason.cause)
                return@Runnable
            }
            player.sendMessage("${ChatColor.GREEN}Currency description updated.")
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