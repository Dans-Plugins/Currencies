package com.dansplugins.currencies.command.currency.mint

import com.dansplugins.currencies.Currencies
import com.dansplugins.currencies.currency.Currency
import com.dansplugins.currencies.currency.CurrencyId
import com.dansplugins.currencies.currency.CurrencyStatus.RETIRED
import com.dansplugins.factionsystem.faction.MfFaction
import com.dansplugins.factionsystem.faction.MfFactionId
import com.dansplugins.factionsystem.faction.role.MfFactionRole
import com.dansplugins.factionsystem.failure.ServiceFailure
import com.dansplugins.factionsystem.failure.ServiceFailureType.GENERAL
import com.dansplugins.factionsystem.player.MfPlayer
import com.dansplugins.factionsystem.player.MfPlayerId
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.bukkit.ChatColor.GREEN
import org.bukkit.ChatColor.RED
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class CurrencyMintCommandTest {

    private fun goldIngot() = mockk<ItemStack>(relaxed = true) {
        every { hasItemMeta() } returns false
        every { type } returns Material.GOLD_INGOT
        every { amount } returns 1
    }

    private val gold = Currency(id = CurrencyId("gold"), factionId = MfFactionId("faction"), name = "Gold", item = goldIngot(), amount = 100)
    private val shinyGold = Currency(id = CurrencyId("shiny-gold"), factionId = MfFactionId("faction"), name = "Shiny Gold", item = goldIngot())
    private val silver = Currency(id = CurrencyId("silver"), factionId = MfFactionId("faction"), name = "Silver", item = goldIngot(), status = RETIRED)

    private val plugin = mockk<Currencies>(relaxed = true) {
        every { name } returns "currencies"
    }
    private val currencyService = plugin.services.currencyService
    private val playerService = plugin.medievalFactions.services.playerService
    private val bukkitCommand = mockk<Command>(relaxed = true)
    private val mintCommand = CurrencyMintCommand(plugin)

    private val playerUuid = UUID.randomUUID()
    private val mfPlayer = MfPlayer(id = MfPlayerId(playerUuid.toString()), name = "Minter", power = 10.0)

    /**
     * The command hops between the scheduler's async and main threads three times, so every
     * scheduled task is run as soon as it is scheduled rather than only asserting that it was.
     */
    @BeforeEach
    fun runScheduledTasksInline() {
        every { plugin.server.scheduler.runTaskAsynchronously(plugin, any<Runnable>()) } answers {
            secondArg<Runnable>().run()
            mockk(relaxed = true)
        }
        every { plugin.server.scheduler.runTask(plugin, any<Runnable>()) } answers {
            secondArg<Runnable>().run()
            mockk(relaxed = true)
        }
        every { playerService.getPlayer(any<MfPlayerId>()) } returns mfPlayer
        every { currencyService.save(any()) } answers { Success(firstArg()) }
    }

    private val inventory = mockk<PlayerInventory>(relaxed = true) {
        every { addItem(*anyVararg()) } returns hashMapOf()
    }

    private fun player(permitted: Boolean = true) = mockk<Player>(relaxed = true) {
        every { hasPermission("currencies.mint") } returns permitted
        every { uniqueId } returns playerUuid
        every { inventory } returns this@CurrencyMintCommandTest.inventory
    }

    private fun messagesSentBy(sender: CommandSender, vararg args: String): List<String> {
        val messages = mutableListOf<String>()
        every { sender.sendMessage(capture(messages)) } returns Unit
        assertTrue(mintCommand.onCommand(sender, bukkitCommand, "mint", args))
        return messages
    }

    /**
     * Stubs the currency lookup for both of the service's overloads, since the command tries the
     * first argument as a currency id before falling back to a joined currency name.
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

    private fun config(powerCostEnabled: Boolean = false, powerCost: Double = 0.0, itemCostEnabled: Boolean = false) {
        every { plugin.config.getBoolean("currencies.powerCostEnabled") } returns powerCostEnabled
        every { plugin.config.getDouble("currencies.powerCost") } returns powerCost
        every { plugin.config.getBoolean("currencies.itemCostEnabled") } returns itemCostEnabled
    }

    private fun itemsGiven(): List<ItemStack> {
        val given = mutableListOf<ItemStack>()
        verify { inventory.addItem(capture(given)) }
        return given
    }

    private fun savedCurrency(): Currency {
        val saved = slot<Currency>()
        verify { currencyService.save(capture(saved)) }
        return saved.captured
    }

    @Test
    fun `senders without the mint permission are refused`() {
        assertEquals(
            listOf("${RED}You do not have permission to mint currencies."),
            messagesSentBy(player(permitted = false), "Gold")
        )
    }

    @Test
    fun `non-players are refused even when permitted`() {
        val console = mockk<CommandSender>(relaxed = true) {
            every { hasPermission("currencies.mint") } returns true
        }
        assertEquals(
            listOf("${RED}You must be a player to mint currencies."),
            messagesSentBy(console, "Gold")
        )
    }

    @Test
    fun `usage message is sent when no currency is given`() {
        assertEquals(
            listOf("${RED}Usage: /currency mint [currency] (amount)"),
            messagesSentBy(player())
        )
    }

    @Test
    fun `an unrecognised currency is reported and nothing is minted`() {
        currencyLookupReturns(gold)
        val sender = player()
        assertEquals(
            listOf("${RED}There is no currency by that name."),
            messagesSentBy(sender, "Bronze", "5")
        )
        verify(exactly = 0) { inventory.addItem(*anyVararg()) }
        verify(exactly = 0) { currencyService.save(any()) }
    }

    @Test
    fun `a retired currency cannot be minted`() {
        currencyLookupReturns(silver)
        factionOwning(silver)
        assertEquals(
            listOf("${RED}That currency is no longer active."),
            messagesSentBy(player(), "Silver")
        )
        verify(exactly = 0) { currencyService.save(any()) }
    }

    @Test
    fun `a player in a different faction is refused`() {
        currencyLookupReturns(gold)
        factionOwning(gold, factionId = MfFactionId("other-faction"))
        assertEquals(
            listOf("${RED}Your role in this faction does not give you permission to mint this currency."),
            messagesSentBy(player(), "Gold")
        )
    }

    @Test
    fun `a player in no faction is refused`() {
        currencyLookupReturns(gold)
        every { plugin.medievalFactions.services.factionService.getFaction(any<MfPlayerId>()) } returns null
        assertEquals(
            listOf("${RED}Your role in this faction does not give you permission to mint this currency."),
            messagesSentBy(player(), "Gold")
        )
    }

    @Test
    fun `a player with no role in the owning faction is refused`() {
        currencyLookupReturns(gold)
        factionOwning(gold, role = null)
        assertEquals(
            listOf("${RED}Your role in this faction does not give you permission to mint this currency."),
            messagesSentBy(player(), "Gold")
        )
    }

    @Test
    fun `a player whose role lacks the mint permission is refused`() {
        currencyLookupReturns(gold)
        factionOwning(gold, role = mockk(relaxed = true) { every { hasPermission(any(), any()) } returns false })
        val sender = player()
        assertEquals(
            listOf("${RED}Your role in this faction does not give you permission to mint this currency."),
            messagesSentBy(sender, "Gold")
        )
        verify(exactly = 0) { inventory.addItem(*anyVararg()) }
        verify(exactly = 0) { currencyService.save(any()) }
    }

    @Test
    fun `minting without an amount mints one coin`() {
        config()
        currencyLookupReturns(gold)
        factionOwning(gold)
        val sender = player()
        assertEquals(listOf("${GREEN}Minted 1 x Gold."), messagesSentBy(sender, "Gold"))
        assertEquals(listOf(1), itemsGiven().map(ItemStack::getAmount))
        assertEquals(gold.copy(amount = 101), savedCurrency())
    }

    @Test
    fun `minting an amount gives that many coins and adds them to the minted total`() {
        config()
        currencyLookupReturns(gold)
        factionOwning(gold)
        val sender = player()
        assertEquals(listOf("${GREEN}Minted 10 x Gold."), messagesSentBy(sender, "Gold", "10"))
        val given = itemsGiven().single()
        assertEquals(Material.GOLD_INGOT, given.type)
        assertEquals(10, given.amount)
        assertEquals(gold.copy(amount = 110), savedCurrency())
    }

    @Test
    fun `a currency id is resolved`() {
        config()
        currencyLookupReturns(shinyGold)
        factionOwning(shinyGold)
        assertEquals(listOf("${GREEN}Minted 3 x Shiny Gold."), messagesSentBy(player(), "shiny-gold", "3"))
        assertEquals(shinyGold.copy(amount = 3), savedCurrency())
    }

    @Test
    fun `a multi-word currency name is resolved with the trailing amount removed`() {
        config()
        currencyLookupReturns(shinyGold)
        factionOwning(shinyGold)
        assertEquals(listOf("${GREEN}Minted 3 x Shiny Gold."), messagesSentBy(player(), "Shiny", "Gold", "3"))
        assertEquals(shinyGold.copy(amount = 3), savedCurrency())
    }

    @Test
    fun `a multi-word currency name without an amount mints one coin`() {
        config()
        currencyLookupReturns(shinyGold)
        factionOwning(shinyGold)
        assertEquals(listOf("${GREEN}Minted 1 x Shiny Gold."), messagesSentBy(player(), "Shiny", "Gold"))
        assertEquals(shinyGold.copy(amount = 1), savedCurrency())
    }

    /**
     * Bukkit splits on spaces before the plugin sees the arguments, so the quoted form arrives as
     * separate arguments with the quote characters still attached.
     */
    @Test
    fun `a quoted multi-word currency name is resolved`() {
        config()
        currencyLookupReturns(shinyGold)
        factionOwning(shinyGold)
        assertEquals(listOf("${GREEN}Minted 2 x Shiny Gold."), messagesSentBy(player(), "\"Shiny", "Gold\"", "2"))
        assertEquals(shinyGold.copy(amount = 2), savedCurrency())
    }

    @Test
    fun `coins that do not fit in the inventory are dropped at the player's feet`() {
        config()
        currencyLookupReturns(gold)
        factionOwning(gold)
        val overflow = mockk<ItemStack>()
        val location = mockk<Location>()
        val sender = player()
        every { inventory.addItem(*anyVararg()) } returns hashMapOf(0 to overflow)
        every { sender.location } returns location
        messagesSentBy(sender, "Gold", "64")
        verify { sender.world.dropItem(location, overflow) }
    }

    @Test
    fun `a player without enough power is refused when minting costs power`() {
        config(powerCostEnabled = true, powerCost = 2.0)
        currencyLookupReturns(gold)
        factionOwning(gold)
        val sender = player()
        assertEquals(
            listOf("${RED}You need at least 12.0 power to mint 6 Gold"),
            messagesSentBy(sender, "Gold", "6")
        )
        verify(exactly = 0) { inventory.addItem(*anyVararg()) }
        verify(exactly = 0) { playerService.save(any()) }
        verify(exactly = 0) { currencyService.save(any()) }
    }

    @Test
    fun `minting deducts power per coin when minting costs power`() {
        config(powerCostEnabled = true, powerCost = 2.0)
        currencyLookupReturns(gold)
        factionOwning(gold)
        val savedPlayer = slot<MfPlayer>()
        every { playerService.save(capture(savedPlayer)) } answers { Success(savedPlayer.captured) }
        assertEquals(listOf("${GREEN}Minted 5 x Gold."), messagesSentBy(player(), "Gold", "5"))
        assertEquals(mfPlayer.copy(power = 0.0), savedPlayer.captured)
        assertEquals(gold.copy(amount = 105), savedCurrency())
    }

    @Test
    fun `a failure to save the player's power leaves the minted total unchanged`() {
        config(powerCostEnabled = true, powerCost = 1.0)
        currencyLookupReturns(gold)
        factionOwning(gold)
        every { playerService.save(any()) } returns Failure(ServiceFailure(GENERAL, "database unavailable", Exception("database unavailable")))
        assertEquals(listOf("${RED}Failed to save player information."), messagesSentBy(player(), "Gold", "1"))
        verify(exactly = 0) { currencyService.save(any()) }
    }

    @Test
    fun `a player without enough items is refused when minting costs items`() {
        config(itemCostEnabled = true)
        currencyLookupReturns(gold)
        factionOwning(gold)
        val sender = player()
        every { inventory.containsAtLeast(any(), 4) } returns false
        assertEquals(
            listOf("${RED}You need at least 4 x gold ingot to mint 4 Gold"),
            messagesSentBy(sender, "Gold", "4")
        )
        verify(exactly = 0) { inventory.addItem(*anyVararg()) }
        verify(exactly = 0) { currencyService.save(any()) }
    }

    @Test
    fun `minting consumes one of the currency's item type per coin when minting costs items`() {
        config(itemCostEnabled = true)
        currencyLookupReturns(gold)
        factionOwning(gold)
        val sender = player()
        every { inventory.containsAtLeast(any(), 4) } returns true
        val removed = mutableListOf<ItemStack>()
        every { inventory.removeItem(capture(removed)) } returns hashMapOf()
        assertEquals(listOf("${GREEN}Minted 4 x Gold."), messagesSentBy(sender, "Gold", "4"))
        // ItemStack equality consults the server's item factory, which is absent under test.
        assertEquals(listOf(Material.GOLD_INGOT to 4), removed.map { it.type to it.amount })
        assertEquals(gold.copy(amount = 104), savedCurrency())
    }

    @Test
    fun `no items are consumed when minting does not cost items`() {
        config(itemCostEnabled = false)
        currencyLookupReturns(gold)
        factionOwning(gold)
        val sender = player()
        messagesSentBy(sender, "Gold", "4")
        verify(exactly = 0) { inventory.removeItem(*anyVararg()) }
    }

    @Test
    fun `a failure to save the currency leaves the sender told the save failed`() {
        config()
        currencyLookupReturns(gold)
        factionOwning(gold)
        every { currencyService.save(any()) } returns Failure(ServiceFailure(GENERAL, "database unavailable", Exception("database unavailable")))
        assertEquals(listOf("${RED}Failed to save currency."), messagesSentBy(player(), "Gold"))
    }

    /**
     * Characterizes current behavior rather than intended behavior: a non-positive amount is not
     * rejected, so the minted total goes down and, when minting costs power, the player gains power.
     * Tracked as a bug in #225; this test should change when that is fixed.
     */
    @Test
    fun `a negative amount is currently accepted and credits power back`() {
        config(powerCostEnabled = true, powerCost = 2.0)
        currencyLookupReturns(gold)
        factionOwning(gold)
        val savedPlayer = slot<MfPlayer>()
        every { playerService.save(capture(savedPlayer)) } answers { Success(savedPlayer.captured) }
        assertEquals(listOf("${GREEN}Minted -5 x Gold."), messagesSentBy(player(), "Gold", "-5"))
        assertEquals(mfPlayer.copy(power = 20.0), savedPlayer.captured)
        assertEquals(gold.copy(amount = 95), savedCurrency())
    }

    @Test
    fun `every currency is offered by tab completion`() {
        every { currencyService.currencies } returns listOf(gold, shinyGold, silver)
        assertEquals(
            listOf("Gold", "Shiny Gold", "Silver"),
            mintCommand.onTabComplete(mockk(relaxed = true), bukkitCommand, "mint", emptyArray())
        )
    }

    @Test
    fun `tab completion filters currencies by the partial argument`() {
        every { currencyService.currencies } returns listOf(gold, shinyGold, silver)
        assertEquals(
            listOf("Shiny Gold", "Silver"),
            mintCommand.onTabComplete(mockk(relaxed = true), bukkitCommand, "mint", arrayOf("s"))
        )
        assertEquals(
            emptyList<String>(),
            mintCommand.onTabComplete(mockk(relaxed = true), bukkitCommand, "mint", arrayOf("Gold", "1"))
        )
    }
}
