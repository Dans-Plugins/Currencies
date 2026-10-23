package com.dansplugins.currencies.command.coinpurse

import com.dansplugins.currencies.Currencies
import com.dansplugins.currencies.balance.Balance
import com.dansplugins.currencies.coinpurse.CoinpurseInventoryHolder
import com.dansplugins.currencies.currency.Currency
import com.dansplugins.currencies.currency.CurrencyId
import com.dansplugins.factionsystem.faction.MfFactionId
import com.dansplugins.factionsystem.player.MfPlayerId
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.bukkit.ChatColor.RED
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class CoinpurseCommandTest {

    private fun item(material: Material) = mockk<ItemStack>(relaxed = true) {
        every { hasItemMeta() } returns false
        every { type } returns material
        every { amount } returns 1
    }

    private val gold = Currency(id = CurrencyId("gold"), factionId = MfFactionId("faction"), name = "Gold", item = item(Material.GOLD_INGOT))
    private val pearls = Currency(id = CurrencyId("pearls"), factionId = MfFactionId("faction"), name = "Pearls", item = item(Material.ENDER_PEARL))

    private val plugin = mockk<Currencies>(relaxed = true) {
        every { name } returns "currencies"
    }
    private val currencyService = plugin.services.currencyService
    private val balanceService = plugin.services.balanceService
    private val bukkitCommand = mockk<Command>(relaxed = true)
    private val coinpurseCommand = CoinpurseCommand(plugin)

    private val location = mockk<Location>()
    private val sender = mockk<Player>(relaxed = true) {
        every { hasPermission("currencies.coinpurse") } returns true
        every { uniqueId } returns UUID.randomUUID()
        every { location } returns this@CoinpurseCommandTest.location
    }

    /**
     * A stand-in for the coinpurse inventory with a fixed number of free slots. Each stack is
     * recorded as its type and amount at the moment it is added or dropped, because the command
     * reuses and mutates stacks, and ItemStack equality consults the server's item factory, which is
     * absent under test.
     */
    private var freeSlots = 54
    private val contents = mutableListOf<Pair<Material, Int>>()
    private val dropped = mutableListOf<Pair<Material, Int>>()
    private val inventory = mockk<Inventory>(relaxed = true)

    @BeforeEach
    fun setUp() {
        every { plugin.server.scheduler.runTaskAsynchronously(plugin, any<Runnable>()) } answers {
            secondArg<Runnable>().run()
            mockk(relaxed = true)
        }
        every { plugin.server.scheduler.runTask(plugin, any<Runnable>()) } answers {
            secondArg<Runnable>().run()
            mockk(relaxed = true)
        }
        every { plugin.server.createInventory(any(), any<Int>(), any<String>()) } returns inventory
        every { inventory.addItem(*anyVararg()) } answers {
            val leftOver = hashMapOf<Int, ItemStack>()
            firstArg<Array<ItemStack>>().forEachIndexed { index, stack ->
                if (contents.size < freeSlots) {
                    contents += stack.type to stack.amount
                } else {
                    leftOver[index] = stack
                }
            }
            leftOver
        }
        every { inventory.removeItem(*anyVararg()) } answers {
            firstArg<Array<ItemStack>>().forEach { stack ->
                var toRemove = stack.amount
                contents.indices.reversed().forEach { slot ->
                    val (type, amount) = contents[slot]
                    if (type == stack.type && toRemove > 0) {
                        val removed = minOf(amount, toRemove)
                        contents[slot] = type to amount - removed
                        toRemove -= removed
                    }
                }
            }
            contents.removeAll { (_, amount) -> amount == 0 }
            hashMapOf()
        }
        every { sender.world.dropItem(location, any()) } answers {
            val stack = secondArg<ItemStack>()
            dropped += stack.type to stack.amount
            mockk(relaxed = true)
        }
    }

    private fun balancesOf(vararg balances: Pair<Currency, Int>) {
        val playerId = MfPlayerId.fromBukkitPlayer(sender)
        every { balanceService.getBalances(any()) } returns balances.map { (currency, balance) ->
            Balance(playerId, currency.id, balance = balance)
        }
        // The CurrencyId value class is erased to its String value by the time mockk sees the argument.
        every { currencyService.getCurrency(any<CurrencyId>()) } answers {
            balances.map { it.first }.singleOrNull { it.id.value == firstArg<String>() }
        }
    }

    private fun messagesSentBy(sender: CommandSender): List<String> {
        val messages = mutableListOf<String>()
        every { sender.sendMessage(capture(messages)) } returns Unit
        assertTrue(coinpurseCommand.onCommand(sender, bukkitCommand, "coinpurse", emptyArray()))
        return messages
    }

    private fun total(stacks: List<Pair<Material, Int>>, material: Material) =
        stacks.filter { (type, _) -> type == material }.sumOf { (_, amount) -> amount }

    @Test
    fun `senders without the coinpurse permission are refused`() {
        every { sender.hasPermission("currencies.coinpurse") } returns false
        assertEquals(listOf("${RED}You do not have permission to open your coinpurse."), messagesSentBy(sender))
        verify(exactly = 0) { sender.openInventory(any<Inventory>()) }
    }

    @Test
    fun `non-players are refused even when permitted`() {
        val console = mockk<CommandSender>(relaxed = true) {
            every { hasPermission("currencies.coinpurse") } returns true
        }
        assertEquals(listOf("${RED}You must be a player to open your coinpurse."), messagesSentBy(console))
    }

    @Test
    fun `the coinpurse is opened for the player`() {
        balancesOf(gold to 3)
        assertEquals(emptyList<String>(), messagesSentBy(sender))
        verify { sender.openInventory(inventory) }
    }

    @Test
    fun `the coinpurse is sized by the configured number of slots`() {
        every { plugin.config.getInt("coinpurse.slots") } returns 27
        balancesOf()
        messagesSentBy(sender)
        verify { plugin.server.createInventory(any<CoinpurseInventoryHolder>(), 27, "Coinpurse") }
    }

    @Test
    fun `a balance is placed in the coinpurse as full stacks and a partial stack`() {
        balancesOf(gold to 130)
        messagesSentBy(sender)
        assertEquals(listOf(Material.GOLD_INGOT to 64, Material.GOLD_INGOT to 64, Material.GOLD_INGOT to 2), contents)
        assertEquals(emptyList<Pair<Material, Int>>(), dropped)
    }

    @Test
    fun `a balance that is a whole number of stacks leaves no partial stack`() {
        balancesOf(gold to 128)
        messagesSentBy(sender)
        assertEquals(listOf(Material.GOLD_INGOT to 64, Material.GOLD_INGOT to 64), contents)
    }

    @Test
    fun `stacks are sized by the currency item's own maximum stack size`() {
        balancesOf(pearls to 20)
        messagesSentBy(sender)
        assertEquals(listOf(Material.ENDER_PEARL to 16, Material.ENDER_PEARL to 4), contents)
    }

    @Test
    fun `every currency the player holds is placed in the coinpurse`() {
        balancesOf(gold to 5, pearls to 3)
        messagesSentBy(sender)
        assertEquals(5, total(contents, Material.GOLD_INGOT))
        assertEquals(3, total(contents, Material.ENDER_PEARL))
    }

    @Test
    fun `a zero balance places nothing in the coinpurse`() {
        balancesOf(gold to 0)
        messagesSentBy(sender)
        assertEquals(emptyList<Pair<Material, Int>>(), contents)
        verify(exactly = 0) { inventory.addItem(*anyVararg()) }
    }

    @Test
    fun `a balance for a currency that no longer exists is skipped`() {
        val playerId = MfPlayerId.fromBukkitPlayer(sender)
        every { balanceService.getBalances(any()) } returns listOf(Balance(playerId, CurrencyId("deleted"), balance = 10))
        every { currencyService.getCurrency(any<CurrencyId>()) } returns null
        messagesSentBy(sender)
        assertEquals(emptyList<Pair<Material, Int>>(), contents)
        verify { sender.openInventory(inventory) }
    }

    @Test
    fun `coins that do not fit in the coinpurse are dropped at the player's feet`() {
        freeSlots = 1
        balancesOf(gold to 70)
        messagesSentBy(sender)
        assertEquals(listOf(Material.GOLD_INGOT to 64), contents)
        assertEquals(listOf(Material.GOLD_INGOT to 6), dropped)
    }

    @Test
    fun `no more coins are dropped than the balance holds when the coinpurse is full`() {
        freeSlots = 1
        balancesOf(gold to 64, pearls to 10)
        messagesSentBy(sender)
        assertEquals(64, total(contents, Material.GOLD_INGOT))
        assertEquals(listOf(Material.ENDER_PEARL to 10), dropped)
    }

    @Test
    fun `tab completion offers nothing`() {
        assertEquals(emptyList<String>(), coinpurseCommand.onTabComplete(sender, bukkitCommand, "coinpurse", emptyArray()))
    }
}
