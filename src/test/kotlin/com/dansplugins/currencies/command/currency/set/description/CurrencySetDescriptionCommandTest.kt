package com.dansplugins.currencies.command.currency.set.description

import com.dansplugins.currencies.Currencies
import com.dansplugins.currencies.currency.Currency
import com.dansplugins.currencies.currency.CurrencyId
import com.dansplugins.factionsystem.faction.MfFaction
import com.dansplugins.factionsystem.faction.MfFactionId
import com.dansplugins.factionsystem.faction.role.MfFactionRole
import com.dansplugins.factionsystem.failure.ServiceFailure
import com.dansplugins.factionsystem.failure.ServiceFailureType.GENERAL
import com.dansplugins.factionsystem.player.MfPlayerId
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.bukkit.ChatColor.GREEN
import org.bukkit.ChatColor.RED
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.conversations.Conversation
import org.bukkit.entity.Player
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

class CurrencySetDescriptionCommandTest {

    private val gold = Currency(id = CurrencyId("gold"), factionId = MfFactionId("faction"), name = "Gold", description = "Old words", item = mockk(relaxed = true))
    private val shinyGold = Currency(id = CurrencyId("shiny-gold"), factionId = MfFactionId("faction"), name = "Shiny Gold", item = mockk(relaxed = true))
    private val silver = Currency(id = CurrencyId("silver"), factionId = MfFactionId("faction"), name = "Silver", item = mockk(relaxed = true))

    private val plugin = mockk<Currencies>(relaxed = true) {
        every { name } returns "currencies"
    }
    private val currencyService = plugin.services.currencyService
    private val bukkitCommand = mockk<Command>(relaxed = true)
    private val setDescriptionCommand = CurrencySetDescriptionCommand(plugin)

    private fun player(permitted: Boolean = true, forced: Boolean = false) = mockk<Player>(relaxed = true) {
        every { hasPermission("currencies.desc") } returns permitted
        every { hasPermission("currencies.force.desc") } returns forced
        every { uniqueId } returns UUID.randomUUID()
    }

    /**
     * The description is saved on the scheduler's async thread, so the scheduled task is captured and
     * run here rather than only asserting that something was scheduled. The follow-up `/currency info`
     * is scheduled back onto the main thread, and is captured and run the same way.
     */
    private fun messagesSentBy(sender: CommandSender, vararg args: String): List<String> {
        val messages = mutableListOf<String>()
        every { sender.sendMessage(capture(messages)) } returns Unit
        captureAndRunScheduledTasks { assertTrue(setDescriptionCommand.onCommand(sender, bukkitCommand, "description", args)) }
        return messages
    }

    private fun captureAndRunScheduledTasks(action: () -> Unit) {
        val asyncTask = slot<Runnable>()
        val syncTask = slot<Runnable>()
        every { plugin.server.scheduler.runTaskAsynchronously(plugin, capture(asyncTask)) } returns mockk(relaxed = true)
        every { plugin.server.scheduler.runTask(plugin, capture(syncTask)) } returns mockk(relaxed = true)
        action()
        if (asyncTask.isCaptured) asyncTask.captured.run()
        if (syncTask.isCaptured) syncTask.captured.run()
    }

    /**
     * Stubs the currency lookup for both of the service's overloads, matching the real service: the
     * id lookup is exact and the name lookup ignores case.
     */
    private fun currencyLookupReturns(vararg currencies: Currency) {
        // The CurrencyId value class is erased to its String value by the time mockk sees the argument.
        every { currencyService.getCurrency(any<CurrencyId>()) } answers { currencies.singleOrNull { it.id.value == firstArg<String>() } }
        every { currencyService.getCurrency(any<String>()) } answers { currencies.singleOrNull { it.name.equals(firstArg<String>(), ignoreCase = true) } }
    }

    private fun playerFaction(
        factionId: MfFactionId = MfFactionId("faction"),
        role: MfFactionRole? = mockk(relaxed = true) { every { hasPermission(any(), any()) } returns true }
    ): MfFaction {
        val faction = mockk<MfFaction>(relaxed = true) {
            every { id } returns factionId
            every { getRole(any<MfPlayerId>()) } returns role
        }
        every { plugin.medievalFactions.services.factionService.getFaction(any<MfPlayerId>()) } returns faction
        return faction
    }

    private fun savesSucceed(): CapturingSlot<Currency> {
        val saved = slot<Currency>()
        every { currencyService.save(capture(saved)) } answers { Success(saved.captured) }
        return saved
    }

    @Test
    fun `senders without the description permission are refused`() {
        assertEquals(
            listOf("${RED}You do not have permission to change the description of currencies."),
            messagesSentBy(player(permitted = false), "Gold", "Shiny")
        )
        verify(exactly = 0) { currencyService.save(any()) }
    }

    @Test
    fun `non-players are refused even when permitted`() {
        val console = mockk<CommandSender>(relaxed = true) {
            every { hasPermission("currencies.desc") } returns true
        }
        assertEquals(
            listOf("${RED}You must be a player to change the description of currencies."),
            messagesSentBy(console, "Gold", "Shiny")
        )
    }

    @Test
    fun `usage message is sent when no currency is given`() {
        assertEquals(
            listOf("${RED}Usage: /currency set description [currency name] (new description)"),
            messagesSentBy(player())
        )
    }

    @Test
    fun `an unrecognised currency is reported`() {
        currencyLookupReturns(gold)
        playerFaction()
        assertEquals(
            listOf("${RED}There is no currency by that name."),
            messagesSentBy(player(), "Bronze", "Shiny")
        )
        verify(exactly = 0) { currencyService.save(any()) }
    }

    @Test
    fun `a player in a different faction is refused`() {
        currencyLookupReturns(gold)
        playerFaction(factionId = MfFactionId("other-faction"))
        assertEquals(
            listOf("${RED}Your role in this faction does not give you permission to change the description of this currency."),
            messagesSentBy(player(), "Gold", "Shiny")
        )
        verify(exactly = 0) { currencyService.save(any()) }
    }

    @Test
    fun `a player in no faction is refused`() {
        currencyLookupReturns(gold)
        every { plugin.medievalFactions.services.factionService.getFaction(any<MfPlayerId>()) } returns null
        assertEquals(
            listOf("${RED}Your role in this faction does not give you permission to change the description of this currency."),
            messagesSentBy(player(), "Gold", "Shiny")
        )
        verify(exactly = 0) { currencyService.save(any()) }
    }

    @Test
    fun `a player with no role in the owning faction is refused`() {
        currencyLookupReturns(gold)
        playerFaction(role = null)
        assertEquals(
            listOf("${RED}Your role in this faction does not give you permission to change the description of this currency."),
            messagesSentBy(player(), "Gold", "Shiny")
        )
        verify(exactly = 0) { currencyService.save(any()) }
    }

    @Test
    fun `a player whose role lacks the change-description permission is refused`() {
        currencyLookupReturns(gold)
        playerFaction(role = mockk(relaxed = true) { every { hasPermission(any(), any()) } returns false })
        assertEquals(
            listOf("${RED}Your role in this faction does not give you permission to change the description of this currency."),
            messagesSentBy(player(), "Gold", "Shiny")
        )
        verify(exactly = 0) { currencyService.save(any()) }
    }

    @Test
    fun `setting the description saves it and shows the currency's info`() {
        currencyLookupReturns(gold)
        playerFaction()
        val saved = savesSucceed()
        val sender = player()
        assertEquals(
            listOf("${GREEN}Currency description updated."),
            messagesSentBy(sender, "Gold", "Shiny")
        )
        assertEquals(gold.copy(description = "Shiny"), saved.captured)
        verify { sender.performCommand("currency info gold") }
    }

    @Test
    fun `the currency may be given by its id`() {
        currencyLookupReturns(shinyGold)
        playerFaction()
        val saved = savesSucceed()
        assertEquals(
            listOf("${GREEN}Currency description updated."),
            messagesSentBy(player(), "shiny-gold", "Shiny")
        )
        assertEquals(shinyGold.copy(description = "Shiny"), saved.captured)
    }

    @Test
    fun `every argument after the currency is joined into the description`() {
        currencyLookupReturns(gold)
        playerFaction()
        val saved = savesSucceed()
        messagesSentBy(player(), "Gold", "The", "official", "coin")
        assertEquals(gold.copy(description = "The official coin"), saved.captured)
    }

    /**
     * Bukkit splits on spaces before the plugin sees the arguments, so the quoted form arrives as
     * separate arguments with the quote characters still attached.
     */
    @Test
    fun `a quoted multi-word currency name and description are resolved`() {
        currencyLookupReturns(shinyGold)
        playerFaction()
        val saved = savesSucceed()
        assertEquals(
            listOf("${GREEN}Currency description updated."),
            messagesSentBy(player(), "\"Shiny", "Gold\"", "\"The", "official", "coin\"")
        )
        assertEquals(shinyGold.copy(description = "The official coin"), saved.captured)
    }

    /**
     * Characterizes current behaviour: without quotes only the first argument names the currency, so
     * an unquoted multi-word name is not resolved.
     */
    @Test
    fun `an unquoted multi-word currency name is not resolved`() {
        currencyLookupReturns(shinyGold)
        playerFaction()
        assertEquals(
            listOf("${RED}There is no currency by that name."),
            messagesSentBy(player(), "Shiny", "Gold", "The", "official", "coin")
        )
        verify(exactly = 0) { currencyService.save(any()) }
    }

    @Test
    fun `the force permission bypasses the faction check`() {
        currencyLookupReturns(gold)
        every { plugin.medievalFactions.services.factionService.getFaction(any<MfPlayerId>()) } returns null
        val saved = savesSucceed()
        assertEquals(
            listOf("${GREEN}Currency description updated."),
            messagesSentBy(player(forced = true), "Gold", "Shiny")
        )
        assertEquals(gold.copy(description = "Shiny"), saved.captured)
    }

    @Test
    fun `a failure to save leaves the sender told the save failed`() {
        currencyLookupReturns(gold)
        playerFaction()
        every { currencyService.save(any()) } returns Failure(ServiceFailure(GENERAL, "database unavailable", Exception("database unavailable")))
        val sender = player()
        assertEquals(
            listOf("${RED}Failed to save currency."),
            messagesSentBy(sender, "Gold", "Shiny")
        )
        verify(exactly = 0) { sender.performCommand(any()) }
    }

    /**
     * Ponder's unquote throws on these, which previously reached the player as an internal error
     * rather than a message (#213). No description is saved either way.
     */
    @Test
    fun `an empty or unbalanced quote is reported as an unrecognised currency`() {
        currencyLookupReturns(gold)
        playerFaction()
        assertEquals(listOf("${RED}There is no currency by that name."), messagesSentBy(player(), "\"\"", "Shiny"))
        assertEquals(listOf("${RED}There is no currency by that name."), messagesSentBy(player(), "\"", "Shiny"))
        verify(exactly = 0) { currencyService.save(any()) }
    }

    /**
     * With no description the command starts a chat prompt for it instead of saving. The prompt is
     * driven here through the conversation the player is handed.
     */
    private fun conversationStartedBy(sender: Player, vararg args: String): Conversation {
        val conversation = slot<Conversation>()
        every { sender.beginConversation(capture(conversation)) } returns true
        assertTrue(setDescriptionCommand.onCommand(sender, bukkitCommand, "description", args))
        assertTrue(conversation.isCaptured, "a conversation was not started")
        return conversation.captured
    }

    @Test
    fun `omitting the description prompts for it instead of saving`() {
        val sender = player()
        val conversation = conversationStartedBy(sender, "Gold")
        assertEquals("Gold", conversation.context.getSessionData("currency"))
        verify(exactly = 0) { plugin.server.scheduler.runTaskAsynchronously(plugin, any<Runnable>()) }
        verify(exactly = 0) { currencyService.save(any()) }
    }

    /**
     * Unlike the rename prompt, the description prompt keeps accepting lines until `end` is typed, and
     * joins them with spaces.
     */
    @Test
    fun `prompted lines are joined into the description when end is typed`() {
        currencyLookupReturns(gold)
        playerFaction()
        val saved = savesSucceed()
        val sender = player()
        val messages = mutableListOf<String>()
        every { sender.sendMessage(capture(messages)) } returns Unit
        val conversation = conversationStartedBy(sender, "Gold")
        conversation.acceptInput("The official coin")
        conversation.acceptInput("of the Northern Kingdom")
        verify(exactly = 0) { currencyService.save(any()) }
        captureAndRunScheduledTasks { conversation.acceptInput("end") }
        assertEquals(gold.copy(description = "The official coin of the Northern Kingdom"), saved.captured)
        assertTrue(messages.contains("${GREEN}Currency description updated."))
        assertFalse(messages.contains("${RED}Operation cancelled."))
    }

    /**
     * Characterizes current behaviour: typing `end` before any line clears the description rather
     * than leaving it unchanged.
     */
    @Test
    fun `typing end before any line clears the description`() {
        currencyLookupReturns(gold)
        playerFaction()
        val saved = savesSucceed()
        val conversation = conversationStartedBy(player(), "Gold")
        captureAndRunScheduledTasks { conversation.acceptInput("end") }
        assertEquals(gold.copy(description = ""), saved.captured)
    }

    @Test
    fun `typing cancel at the prompt abandons the description`() {
        currencyLookupReturns(gold)
        val sender = player()
        val messages = mutableListOf<String>()
        every { sender.sendMessage(capture(messages)) } returns Unit
        val conversation = conversationStartedBy(sender, "Gold")
        conversation.acceptInput("The official coin")
        conversation.acceptInput("cancel")
        assertTrue(messages.contains("${RED}Operation cancelled."))
        verify(exactly = 0) { plugin.server.scheduler.runTaskAsynchronously(plugin, any<Runnable>()) }
        verify(exactly = 0) { currencyService.save(any()) }
    }

    @Test
    fun `every currency is offered by tab completion`() {
        every { currencyService.currencies } returns listOf(gold, shinyGold, silver)
        assertEquals(
            listOf("Gold", "Shiny Gold", "Silver"),
            setDescriptionCommand.onTabComplete(mockk(relaxed = true), bukkitCommand, "description", emptyArray())
        )
    }

    @Test
    fun `tab completion filters currencies by the partial argument and stops after the currency`() {
        every { currencyService.currencies } returns listOf(gold, shinyGold, silver)
        assertEquals(
            listOf("Shiny Gold", "Silver"),
            setDescriptionCommand.onTabComplete(mockk(relaxed = true), bukkitCommand, "description", arrayOf("s"))
        )
        assertEquals(
            emptyList<String>(),
            setDescriptionCommand.onTabComplete(mockk(relaxed = true), bukkitCommand, "description", arrayOf("Gold", "The"))
        )
    }
}
