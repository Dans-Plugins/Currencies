package com.dansplugins.currencies.command.currency.mint

import com.dansplugins.currencies.Currencies
import com.dansplugins.currencies.currency.Currency
import com.dansplugins.currencies.currency.CurrencyId
import com.dansplugins.currencies.currency.CurrencyStatus.RETIRED
import com.dansplugins.factionsystem.faction.MfFaction
import com.dansplugins.factionsystem.faction.MfFactionId
import com.dansplugins.factionsystem.faction.role.MfFactionRole
import com.dansplugins.factionsystem.failure.ServiceFailure
import com.dansplugins.factionsystem.failure.ServiceFailureType.CONFLICT
import com.dansplugins.factionsystem.failure.ServiceFailureType.GENERAL
import com.dansplugins.factionsystem.player.MfPlayer
import com.dansplugins.factionsystem.player.MfPlayerId
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifyOrder
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

    private val saveFailure = Failure(ServiceFailure(GENERAL, "database unavailable", Exception("database unavailable")))

    /** Makes the player service behave like a store: the player read back is the one last saved. */
    private fun storedPlayers(): MutableList<MfPlayer> {
        val saved = mutableListOf<MfPlayer>()
        every { playerService.save(any()) } answers {
            saved += firstArg<MfPlayer>()
            Success(firstArg())
        }
        every { playerService.getPlayer(any<MfPlayerId>()) } answers { saved.lastOrNull() ?: mfPlayer }
        return saved
    }

    /** #296: coins were given before the minted total was saved, so they were kept when it failed. */
    @Test
    fun `no coins are given when the currency cannot be saved`() {
        config()
        currencyLookupReturns(gold)
        factionOwning(gold)
        every { currencyService.save(any()) } returns saveFailure
        assertEquals(listOf("${RED}Failed to save currency."), messagesSentBy(player(), "Gold", "5"))
        verify(exactly = 0) { inventory.addItem(*anyVararg()) }
    }

    /** #296: coins were given before the power cost was saved, so they were kept when it failed. */
    @Test
    fun `no coins are given when the player's power cannot be saved`() {
        config(powerCostEnabled = true, powerCost = 1.0)
        currencyLookupReturns(gold)
        factionOwning(gold)
        every { playerService.save(any()) } returns saveFailure
        assertEquals(listOf("${RED}Failed to save player information."), messagesSentBy(player(), "Gold", "5"))
        verify(exactly = 0) { inventory.addItem(*anyVararg()) }
    }

    /** #296: power was deducted and kept when the minted total then failed to save. */
    @Test
    fun `the power cost is refunded when the currency cannot be saved`() {
        config(powerCostEnabled = true, powerCost = 1.0)
        currencyLookupReturns(gold)
        factionOwning(gold)
        val savedPlayers = storedPlayers()
        every { currencyService.save(any()) } returns saveFailure
        assertEquals(listOf("${RED}Failed to save currency."), messagesSentBy(player(), "Gold", "4"))
        assertEquals(listOf(6.0, 10.0), savedPlayers.map(MfPlayer::power))
        verify(exactly = 0) { inventory.addItem(*anyVararg()) }
    }

    /** #296: with an item cost, the items are taken up front and handed back instead of coins. */
    @Test
    fun `the item cost is handed back and no coins are given when the currency cannot be saved`() {
        config(itemCostEnabled = true)
        currencyLookupReturns(gold)
        factionOwning(gold)
        every { inventory.containsAtLeast(any(), 4) } returns true
        val removed = mutableListOf<ItemStack>()
        every { inventory.removeItem(capture(removed)) } returns hashMapOf()
        every { currencyService.save(any()) } returns saveFailure
        assertEquals(listOf("${RED}Failed to save currency."), messagesSentBy(player(), "Gold", "4"))
        assertEquals(listOf(Material.GOLD_INGOT to 4), removed.map { it.type to it.amount })
        assertEquals(listOf(Material.GOLD_INGOT to 4), itemsGiven().map { it.type to it.amount })
        verify(exactly = 1) { inventory.addItem(*anyVararg()) }
        // Under test a coin stack and a plain stack of the item type look the same, so the refund is
        // told apart from coins by the items being taken before anything is given.
        verifyOrder {
            inventory.removeItem(*anyVararg())
            inventory.addItem(*anyVararg())
        }
    }

    /**
     * #296: the minted total was saved from the snapshot read when the command started, so a mint
     * saved in between left that snapshot's version stale and the save was refused after the coins
     * had been given. The currency is now re-read immediately before it is saved.
     */
    @Test
    fun `a mint saved after the currency was looked up is built on rather than refused`() {
        config()
        factionOwning(gold)
        // The name lookup returns the stale snapshot, while the store holds a newer version.
        var stored = gold.copy(version = 3, amount = 150)
        every { currencyService.getCurrency(any<String>()) } returns gold
        every { currencyService.getCurrency(any<CurrencyId>()) } answers { if (firstArg<String>() == gold.id.value) stored else null }
        every { currencyService.save(any()) } answers {
            val currency = firstArg<Currency>()
            if (currency.version != stored.version) {
                Failure(ServiceFailure(CONFLICT, "Invalid version: ${currency.version}", Exception("Invalid version")))
            } else {
                stored = currency.copy(version = currency.version + 1)
                Success(stored)
            }
        }
        assertEquals(listOf("${GREEN}Minted 5 x Gold."), messagesSentBy(player(), "Gold", "5"))
        assertEquals(155, stored.amount)
        assertEquals(listOf(5), itemsGiven().map(ItemStack::getAmount))
    }

    /** #225: a zero or negative amount credited power back and lowered the minted total. */
    @Test
    fun `a negative amount is rejected before anything is saved`() {
        config(powerCostEnabled = true, powerCost = 2.0)
        currencyLookupReturns(gold)
        factionOwning(gold)
        assertEquals(listOf("${RED}The amount must be at least 1."), messagesSentBy(player(), "Gold", "-5"))
        verify(exactly = 0) { playerService.save(any()) }
        verify(exactly = 0) { currencyService.save(any()) }
    }

    @Test
    fun `a zero amount is rejected before anything is saved`() {
        config(powerCostEnabled = true, powerCost = 2.0)
        currencyLookupReturns(gold)
        factionOwning(gold)
        assertEquals(listOf("${RED}The amount must be at least 1."), messagesSentBy(player(), "Gold", "0"))
        verify(exactly = 0) { playerService.save(any()) }
        verify(exactly = 0) { currencyService.save(any()) }
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
