package com.dansplugins.currencies.command.currency.list

import com.dansplugins.currencies.Currencies
import com.dansplugins.currencies.currency.Currency
import com.dansplugins.currencies.currency.CurrencyId
import com.dansplugins.currencies.currency.CurrencyStatus.ACTIVE
import com.dansplugins.currencies.currency.CurrencyStatus.RETIRED
import com.dansplugins.factionsystem.faction.MfFaction
import com.dansplugins.factionsystem.faction.MfFactionId
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import net.md_5.bungee.api.chat.BaseComponent
import org.bukkit.ChatColor.RED
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CurrencyListCommandTest {

    private val gold = Currency(id = CurrencyId("gold"), factionId = MfFactionId("realm"), name = "Gold", item = mockk(relaxed = true))
    private val shinyGold = Currency(id = CurrencyId("shiny-gold"), factionId = MfFactionId("realm"), name = "Shiny Gold", item = mockk(relaxed = true))
    private val silver = Currency(id = CurrencyId("silver"), factionId = MfFactionId("realm"), name = "Silver", item = mockk(relaxed = true), status = RETIRED)
    private val copper = Currency(id = CurrencyId("copper"), factionId = MfFactionId("guild"), name = "Copper", item = mockk(relaxed = true))

    private val plugin = mockk<Currencies>(relaxed = true) {
        every { name } returns "currencies"
    }
    private val currencyService = plugin.services.currencyService
    private val factionService = plugin.medievalFactions.services.factionService
    private val bukkitCommand = mockk<Command>(relaxed = true)
    private val listCommand = CurrencyListCommand(plugin)

    private val realm = mockk<MfFaction>(relaxed = true) {
        every { id } returns MfFactionId("realm")
        every { name } returns "The Realm"
    }
    private val guild = mockk<MfFaction>(relaxed = true) {
        every { id } returns MfFactionId("guild")
        every { name } returns "Guild"
    }

    @BeforeEach
    fun stubCurrencyAndFactionLookups() {
        val all = listOf(gold, shinyGold, silver, copper)
        every { currencyService.currencies } returns all
        every { currencyService.getCurrencies(ACTIVE) } returns all.filter { it.status == ACTIVE }
        every { currencyService.getCurrencies(RETIRED) } returns all.filter { it.status == RETIRED }
        // The MfFactionId value class is erased to its String value by the time mockk sees the argument.
        every { currencyService.getCurrencies(any<MfFactionId>()) } answers { all.filter { it.factionId.value == firstArg<String>() } }
        every { factionService.getFaction(any<MfFactionId>()) } answers { listOf(realm, guild).singleOrNull { it.id.value == firstArg<String>() } }
        every { factionService.getFaction(any<String>()) } answers { listOf(realm, guild).singleOrNull { it.name.equals(firstArg<String>(), ignoreCase = true) } }
        every { factionService.factions } returns listOf(realm, guild)
    }

    private fun player(permitted: Boolean = true) = mockk<Player>(relaxed = true) {
        every { hasPermission("currencies.list") } returns permitted
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
        assertTrue(listCommand.onCommand(sender, bukkitCommand, "list", args))
        if (task.isCaptured) task.captured.run()
        return messages
    }

    /**
     * Each listed currency is sent as its own clickable line, so the lines that run `/currency info`
     * identify which currencies landed on the page that was sent.
     */
    private fun currencyIdsListedTo(sender: Player): List<String> {
        val components = mutableListOf<BaseComponent>()
        verify(atLeast = 0) { sender.spigot().sendMessage(*varargAll<BaseComponent> { components.add(it); true }) }
        return components
            .mapNotNull { it.clickEvent?.value }
            .filter { it.startsWith("/currency info ") }
            .map { it.removePrefix("/currency info ") }
    }

    @Test
    fun `players without the list permission are refused`() {
        val sender = player(permitted = false)
        assertEquals(
            listOf("${RED}You do not have permission to view the currency list."),
            messagesSentBy(sender)
        )
        verify(exactly = 0) { plugin.server.scheduler.runTaskAsynchronously(any(), any<Runnable>()) }
    }

    @Test
    fun `active currencies are listed when no filter is given`() {
        val sender = player()
        assertEquals(emptyList<String>(), messagesSentBy(sender))
        assertEquals(listOf("gold", "shiny-gold", "copper"), currencyIdsListedTo(sender))
    }

    @Test
    fun `all lists active and retired currencies`() {
        val sender = player()
        assertEquals(emptyList<String>(), messagesSentBy(sender, "all"))
        assertEquals(listOf("gold", "shiny-gold", "silver", "copper"), currencyIdsListedTo(sender))
    }

    @Test
    fun `retired lists only retired currencies`() {
        val sender = player()
        assertEquals(emptyList<String>(), messagesSentBy(sender, "RETIRED"))
        assertEquals(listOf("silver"), currencyIdsListedTo(sender))
    }

    @Test
    fun `a faction's currencies are listed by faction id`() {
        val sender = player()
        assertEquals(emptyList<String>(), messagesSentBy(sender, "guild"))
        assertEquals(listOf("copper"), currencyIdsListedTo(sender))
    }

    @Test
    fun `a multi-word faction name is resolved from the joined arguments`() {
        val sender = player()
        assertEquals(emptyList<String>(), messagesSentBy(sender, "The", "Realm"))
        assertEquals(listOf("gold", "shiny-gold", "silver"), currencyIdsListedTo(sender))
    }

    @Test
    fun `a trailing page number is not treated as part of the faction name`() {
        val sender = player()
        assertEquals(emptyList<String>(), messagesSentBy(sender, "The", "Realm", "1"))
        assertEquals(listOf("gold", "shiny-gold", "silver"), currencyIdsListedTo(sender))
    }

    @Test
    fun `an unrecognised filter is reported`() {
        assertEquals(
            listOf("${RED}Invalid filter, you must specify 'all', 'retired' or a faction name."),
            messagesSentBy(player(), "Nowhere")
        )
    }

    @Test
    fun `an empty result is reported`() {
        every { currencyService.getCurrencies(RETIRED) } returns emptyList()
        assertEquals(
            listOf("${RED}There are currently no currencies."),
            messagesSentBy(player(), "retired")
        )
    }

    @Test
    fun `a page past the end is reported`() {
        assertEquals(listOf("${RED}Invalid page number."), messagesSentBy(player(), "99"))
        assertEquals(listOf("${RED}Invalid page number."), messagesSentBy(player(), "all", "99"))
    }

    @Test
    fun `a page number of zero is reported`() {
        assertEquals(listOf("${RED}Invalid page number."), messagesSentBy(player(), "0"))
    }

    @Test
    fun `tab completion offers the filters and every faction name`() {
        assertEquals(
            listOf("all", "retired", "The Realm", "Guild"),
            listCommand.onTabComplete(mockk(relaxed = true), bukkitCommand, "list", emptyArray())
        )
    }

    @Test
    fun `tab completion filters by the partial argument`() {
        assertEquals(
            listOf("retired"),
            listCommand.onTabComplete(mockk(relaxed = true), bukkitCommand, "list", arrayOf("RE"))
        )
        assertEquals(
            listOf("all"),
            listCommand.onTabComplete(mockk(relaxed = true), bukkitCommand, "list", arrayOf("a"))
        )
        assertEquals(
            emptyList<String>(),
            listCommand.onTabComplete(mockk(relaxed = true), bukkitCommand, "list", arrayOf("all", "2"))
        )
    }
}
