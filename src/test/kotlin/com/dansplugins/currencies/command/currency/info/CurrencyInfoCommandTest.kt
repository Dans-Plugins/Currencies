package com.dansplugins.currencies.command.currency.info

import com.dansplugins.currencies.Currencies
import com.dansplugins.currencies.currency.Currency
import com.dansplugins.currencies.currency.CurrencyId
import com.dansplugins.currencies.currency.CurrencyStatus.RETIRED
import com.dansplugins.currencies.item.tagToNbtJson
import com.dansplugins.factionsystem.faction.MfFactionId
import com.dansplugins.factionsystem.player.MfPlayerId
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import org.bukkit.ChatColor.AQUA
import org.bukkit.ChatColor.GRAY
import org.bukkit.ChatColor.RED
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class CurrencyInfoCommandTest {

    private fun goldIngot() = mockk<ItemStack>(relaxed = true) {
        every { hasItemMeta() } returns false
        every { type } returns Material.GOLD_INGOT
    }

    private val gold = Currency(id = CurrencyId("gold"), factionId = MfFactionId("faction"), name = "Gold", description = "The realm's coin", item = goldIngot(), amount = 42)
    private val shinyGold = Currency(id = CurrencyId("shiny-gold"), factionId = MfFactionId("faction"), name = "Shiny Gold", description = "Polished", item = goldIngot())
    private val silver = Currency(id = CurrencyId("silver"), factionId = MfFactionId("faction"), name = "Silver", item = goldIngot(), status = RETIRED)

    private val plugin = mockk<Currencies>(relaxed = true) {
        every { name } returns "currencies"
    }
    private val currencyService = plugin.services.currencyService
    private val bukkitCommand = mockk<Command>(relaxed = true)
    private val infoCommand = CurrencyInfoCommand(plugin)

    @BeforeEach
    fun stubCollaboratorsOutsideTheCommand() {
        // tagToNbtJson reaches into the running server's NMS classes, which are not present under test.
        mockkStatic("com.dansplugins.currencies.item.ItemStacksKt")
        every { any<ItemStack>().tagToNbtJson() } returns null
        // Neither the sender's faction nor the currency's is resolvable without a server, and their
        // absence is what the command already handles for a player outside the issuing faction.
        every { plugin.medievalFactions.services.factionService.getFaction(any<MfPlayerId>()) } returns null
        every { plugin.medievalFactions.services.factionService.getFaction(any<MfFactionId>()) } returns null
    }

    private fun player() = mockk<Player>(relaxed = true) {
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
        assertTrue(infoCommand.onCommand(sender, bukkitCommand, "info", args))
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

    @Test
    fun `usage message is sent when no currency is given`() {
        assertEquals(
            listOf("${RED}Usage: /currency info [currency]"),
            messagesSentBy(player())
        )
    }

    @Test
    fun `an unrecognised currency is reported`() {
        currencyLookupReturns(gold)
        assertEquals(
            listOf("${RED}There is no currency by that name."),
            messagesSentBy(player(), "Bronze")
        )
    }

    @Test
    fun `a currency is reported by its id`() {
        currencyLookupReturns(gold)
        assertEquals(
            listOf(
                "$AQUA=== Gold ===",
                "${GRAY}Description: The realm's coin",
                "${GRAY}Item: gold ingot"
            ),
            messagesSentBy(player(), "gold")
        )
    }

    @Test
    fun `a retired currency is still reported`() {
        currencyLookupReturns(silver)
        assertEquals(
            listOf(
                "$AQUA=== Silver ===",
                "${GRAY}Description: ",
                "${GRAY}Item: gold ingot"
            ),
            messagesSentBy(player(), "Silver")
        )
    }

    @Test
    fun `a multi-word currency name is resolved from the joined arguments`() {
        currencyLookupReturns(shinyGold)
        assertEquals(
            listOf(
                "$AQUA=== Shiny Gold ===",
                "${GRAY}Description: Polished",
                "${GRAY}Item: gold ingot"
            ),
            messagesSentBy(player(), "Shiny", "Gold")
        )
    }

    /**
     * Bukkit splits on spaces before the plugin sees the arguments, so the quoted form documented in
     * COMMANDS.md arrives as separate arguments with the quote characters still attached.
     */
    @Test
    fun `a quoted multi-word currency name is resolved`() {
        currencyLookupReturns(shinyGold)
        assertEquals(
            listOf(
                "$AQUA=== Shiny Gold ===",
                "${GRAY}Description: Polished",
                "${GRAY}Item: gold ingot"
            ),
            messagesSentBy(player(), "\"Shiny", "Gold\"")
        )
    }

    @Test
    fun `the minted amount is reported only when the config enables it`() {
        currencyLookupReturns(gold)
        every { plugin.config.getBoolean("currencies.showAmountMinted") } returns true
        assertEquals(
            listOf(
                "$AQUA=== Gold ===",
                "${GRAY}Description: The realm's coin",
                "${GRAY}Item: gold ingot",
                "${GRAY}Minted: 42"
            ),
            messagesSentBy(player(), "gold")
        )
    }

    @Test
    fun `non-players may look a currency up`() {
        currencyLookupReturns(gold)
        val console = mockk<CommandSender>(relaxed = true)
        assertEquals(
            listOf(
                "$AQUA=== Gold ===",
                "${GRAY}Description: The realm's coin",
                "${GRAY}Item: gold ingot"
            ),
            messagesSentBy(console, "gold")
        )
    }

    @Test
    fun `every currency is offered by tab completion`() {
        every { currencyService.currencies } returns listOf(gold, shinyGold, silver)
        assertEquals(
            listOf("Gold", "Shiny Gold", "Silver"),
            infoCommand.onTabComplete(mockk(relaxed = true), bukkitCommand, "info", emptyArray())
        )
    }

    @Test
    fun `tab completion filters currencies by the partial argument`() {
        every { currencyService.currencies } returns listOf(gold, shinyGold, silver)
        assertEquals(
            listOf("Shiny Gold", "Silver"),
            infoCommand.onTabComplete(mockk(relaxed = true), bukkitCommand, "info", arrayOf("s"))
        )
        assertEquals(
            emptyList<String>(),
            infoCommand.onTabComplete(mockk(relaxed = true), bukkitCommand, "info", arrayOf("Gold", "co"))
        )
    }
}
