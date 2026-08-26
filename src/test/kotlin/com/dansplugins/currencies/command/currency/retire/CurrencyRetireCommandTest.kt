package com.dansplugins.currencies.command.currency.retire

import com.dansplugins.currencies.Currencies
import com.dansplugins.currencies.currency.Currency
import com.dansplugins.currencies.currency.CurrencyId
import com.dansplugins.currencies.currency.CurrencyStatus.RETIRED
import com.dansplugins.factionsystem.faction.MfFaction
import com.dansplugins.factionsystem.faction.MfFactionId
import com.dansplugins.factionsystem.faction.role.MfFactionRole
import com.dansplugins.factionsystem.failure.ServiceFailure
import com.dansplugins.factionsystem.failure.ServiceFailureType.GENERAL
import com.dansplugins.factionsystem.player.MfPlayerId
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID
import net.md_5.bungee.api.ChatColor as SpigotChatColor
import org.bukkit.ChatColor as BukkitChatColor

class CurrencyRetireCommandTest {

    private val gold = Currency(id = CurrencyId("gold"), factionId = MfFactionId("faction"), name = "Gold", item = mockk(relaxed = true))
    private val shinyGold = Currency(id = CurrencyId("shiny-gold"), factionId = MfFactionId("faction"), name = "Shiny Gold", item = mockk(relaxed = true))
    private val silver = Currency(id = CurrencyId("silver"), factionId = MfFactionId("faction"), name = "Silver", item = mockk(relaxed = true), status = RETIRED)

    private val plugin = mockk<Currencies>(relaxed = true) {
        every { name } returns "currencies"
    }
    private val currencyService = plugin.services.currencyService
    private val bukkitCommand = mockk<Command>(relaxed = true)
    private val retireCommand = CurrencyRetireCommand(plugin)

    private fun player(permitted: Boolean = true) = mockk<Player>(relaxed = true) {
        every { hasPermission("currencies.retire") } returns permitted
        every { uniqueId } returns UUID.randomUUID()
    }

    /**
     * The currency lookup happens on the scheduler's async thread, so the scheduled task is captured
     * and run here rather than only asserting that something was scheduled.
     */
    private fun messagesSentBy(sender: CommandSender, vararg args: String): List<String> {
        val messages = mutableListOf<String>()
        every { sender.sendMessage(capture(messages)) } returns Unit
        val task = slot<Runnable>()
        every { plugin.server.scheduler.runTaskAsynchronously(plugin, capture(task)) } returns mockk(relaxed = true)
        assertTrue(retireCommand.onCommand(sender, bukkitCommand, "retire", args))
        if (task.isCaptured) task.captured.run()
        return messages
    }

    /**
     * Stubs the currency lookup for both of the service's overloads, since the command tries the
     * argument as a currency id before falling back to a joined currency name.
     */
    private fun currencyLookupReturns(vararg currencies: Currency) {
        // The CurrencyId value class is erased to its String value by the time mockk sees the argument.
        every { currencyService.getCurrency(any<CurrencyId>()) } answers { currencies.singleOrNull { it.id.value == firstArg<String>() } }
        every { currencyService.getCurrency(any<String>()) } answers { currencies.singleOrNull { it.name.equals(firstArg<String>(), ignoreCase = true) } }
    }

    private fun factionOwning(
        currency: Currency,
        factionId: MfFactionId = currency.factionId,
        role: MfFactionRole? = mockk(relaxed = true) { every { hasPermission(any(), any()) } returns true }
    ): MfFaction {
        val faction = mockk<MfFaction>(relaxed = true) {
            every { id } returns factionId
            every { getRole(any<MfPlayerId>()) } returns role
        }
        every { plugin.medievalFactions.services.factionService.getFaction(any<MfPlayerId>()) } returns faction
        return faction
    }

    private fun componentSentTo(sender: Player): BaseComponent {
        val component = slot<BaseComponent>()
        verify { sender.spigot().sendMessage(capture(component)) }
        return component.captured
    }

    @Test
    fun `senders without the retire permission are refused`() {
        assertEquals(
            listOf("${BukkitChatColor.RED}You do not have permission to retire currencies."),
            messagesSentBy(player(permitted = false), "Gold")
        )
    }

    @Test
    fun `non-players are refused even when permitted`() {
        val console = mockk<CommandSender>(relaxed = true) {
            every { hasPermission("currencies.retire") } returns true
        }
        assertEquals(
            listOf("${BukkitChatColor.RED}You must be a player to retire currencies."),
            messagesSentBy(console, "Gold")
        )
    }

    @Test
    fun `usage message is sent when no currency is given`() {
        assertEquals(
            listOf("${BukkitChatColor.RED}Usage: /currency retire [currency]"),
            messagesSentBy(player())
        )
    }

    @Test
    fun `an unrecognised currency is reported`() {
        currencyLookupReturns(gold)
        assertEquals(
            listOf("${BukkitChatColor.RED}There is no currency by that name."),
            messagesSentBy(player(), "Bronze")
        )
    }

    @Test
    fun `an already retired currency cannot be retired again`() {
        currencyLookupReturns(silver)
        assertEquals(
            listOf("${BukkitChatColor.RED}That currency is no longer active."),
            messagesSentBy(player(), "Silver", "confirm")
        )
    }

    @Test
    fun `a player in a different faction is refused`() {
        currencyLookupReturns(gold)
        factionOwning(gold, factionId = MfFactionId("other-faction"))
        assertEquals(
            listOf("${BukkitChatColor.RED}Your role in this faction does not give you permission to retire this currency."),
            messagesSentBy(player(), "Gold", "confirm")
        )
    }

    @Test
    fun `a player with no role in the owning faction is refused`() {
        currencyLookupReturns(gold)
        factionOwning(gold, role = null)
        assertEquals(
            listOf("${BukkitChatColor.RED}Your role in this faction does not give you permission to retire this currency."),
            messagesSentBy(player(), "Gold", "confirm")
        )
    }

    @Test
    fun `a player whose role lacks the retire permission is refused`() {
        currencyLookupReturns(gold)
        factionOwning(gold, role = mockk(relaxed = true) { every { hasPermission(any(), any()) } returns false })
        assertEquals(
            listOf("${BukkitChatColor.RED}Your role in this faction does not give you permission to retire this currency."),
            messagesSentBy(player(), "Gold", "confirm")
        )
    }

    @Test
    fun `retiring without confirm prompts instead of retiring`() {
        currencyLookupReturns(gold)
        factionOwning(gold)
        assertEquals(
            listOf("${BukkitChatColor.GRAY}Are you sure you wish to retire Gold? This action is irreversible and will prevent it being minted."),
            messagesSentBy(player(), "Gold")
        )
        verify(exactly = 0) { currencyService.save(any()) }
    }

    @Test
    fun `the confirmation prompt offers a clickable confirm command`() {
        currencyLookupReturns(gold)
        factionOwning(gold)
        val sender = player()
        messagesSentBy(sender, "Gold")
        val component = componentSentTo(sender)
        assertEquals("Confirm", (component as TextComponent).text)
        assertEquals(SpigotChatColor.GREEN, component.color)
        assertEquals(RUN_COMMAND, component.clickEvent.action)
        assertEquals("/currency retire gold confirm", component.clickEvent.value)
    }

    @Test
    fun `confirming retires the currency and notifies the faction`() {
        currencyLookupReturns(gold)
        val faction = factionOwning(gold)
        val saved = slot<Currency>()
        every { currencyService.save(capture(saved)) } answers { Success(saved.captured) }
        assertEquals(
            listOf("${BukkitChatColor.GREEN}Currency retired."),
            messagesSentBy(player(), "Gold", "confirm")
        )
        assertEquals(gold.copy(status = RETIRED), saved.captured)
        verify { faction.sendMessage("Currency retired", "The currency Gold was retired, and will no longer be minted.") }
    }

    @Test
    fun `a multi-word currency name is resolved with the trailing confirm removed`() {
        currencyLookupReturns(shinyGold)
        factionOwning(shinyGold)
        val saved = slot<Currency>()
        every { currencyService.save(capture(saved)) } answers { Success(saved.captured) }
        assertEquals(
            listOf("${BukkitChatColor.GREEN}Currency retired."),
            messagesSentBy(player(), "Shiny", "Gold", "confirm")
        )
        assertEquals(shinyGold.copy(status = RETIRED), saved.captured)
    }

    @Test
    fun `confirm is matched regardless of case`() {
        currencyLookupReturns(gold)
        factionOwning(gold)
        every { currencyService.save(any()) } answers { Success(firstArg()) }
        assertEquals(
            listOf("${BukkitChatColor.GREEN}Currency retired."),
            messagesSentBy(player(), "Gold", "CONFIRM")
        )
    }

    @Test
    fun `a failure to save leaves the sender told the save failed`() {
        currencyLookupReturns(gold)
        val faction = factionOwning(gold)
        every { currencyService.save(any()) } returns Failure(ServiceFailure(GENERAL, "database unavailable", Exception("database unavailable")))
        assertEquals(
            listOf("${BukkitChatColor.RED}Failed to save currency."),
            messagesSentBy(player(), "Gold", "confirm")
        )
        verify(exactly = 0) { faction.sendMessage(any(), any()) }
    }

    @Test
    fun `every currency is offered by tab completion`() {
        every { currencyService.currencies } returns listOf(gold, shinyGold, silver)
        assertEquals(
            listOf("Gold", "Shiny Gold", "Silver"),
            retireCommand.onTabComplete(mockk(relaxed = true), bukkitCommand, "retire", emptyArray())
        )
    }

    @Test
    fun `tab completion filters currencies by the partial argument`() {
        every { currencyService.currencies } returns listOf(gold, shinyGold, silver)
        assertEquals(
            listOf("Shiny Gold", "Silver"),
            retireCommand.onTabComplete(mockk(relaxed = true), bukkitCommand, "retire", arrayOf("s"))
        )
        assertEquals(
            emptyList<String>(),
            retireCommand.onTabComplete(mockk(relaxed = true), bukkitCommand, "retire", arrayOf("Gold", "co"))
        )
    }
}
