package com.dansplugins.currencies.listener

import com.dansplugins.currencies.Currencies
import com.dansplugins.currencies.currency.Currency
import com.dansplugins.currencies.currency.CurrencyId
import com.dansplugins.factionsystem.faction.MfFactionId
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.bukkit.ChatColor.RED
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.inventory.InventoryType.BLAST_FURNACE
import org.bukkit.event.inventory.InventoryType.CHEST
import org.bukkit.event.inventory.InventoryType.FURNACE
import org.bukkit.event.inventory.InventoryType.SMOKER
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class InventoryDragListenerTest {

    private val goldItem = mockk<ItemStack>(relaxed = true)
    private val gold = Currency(id = CurrencyId("gold"), factionId = MfFactionId("faction"), name = "Gold", item = goldItem)

    private val plugin = mockk<Currencies>(relaxed = true)
    private val currencyService = plugin.services.currencyService
    private val listener = InventoryDragListener(plugin)

    private val player = mockk<Player>(relaxed = true)

    @BeforeEach
    fun setUp() {
        every { plugin.config.getBoolean("currencies.disallowSmelting") } returns true
        every { currencyService.currencies } returns listOf(gold)
    }

    // A furnace's top inventory has three slots (0-2); raw slots from 3 upwards are the player's inventory
    private fun drag(type: InventoryType, dragged: ItemStack, rawSlots: Set<Int>): InventoryDragEvent {
        val inventory = mockk<Inventory>(relaxed = true) {
            every { this@mockk.type } returns type
            every { size } returns 3
        }
        val event = mockk<InventoryDragEvent>(relaxed = true) {
            every { this@mockk.inventory } returns inventory
            every { oldCursor } returns dragged
            every { this@mockk.rawSlots } returns rawSlots
            every { whoClicked } returns player
        }
        listener.onInventoryDrag(event)
        return event
    }

    private fun coin() = mockk<ItemStack>(relaxed = true).also {
        every { goldItem.isSimilar(it) } returns true
    }

    @Test
    fun `dragging a currency across furnace slots is cancelled and the player is told why`() {
        val event = drag(FURNACE, coin(), setOf(0, 1))
        verify(exactly = 1) { event.isCancelled = true }
        verify(exactly = 1) { player.sendMessage("${RED}You can't smelt a currency.") }
    }

    @Test
    fun `dragging a currency into a blast furnace is cancelled`() {
        val event = drag(BLAST_FURNACE, coin(), setOf(0))
        verify(exactly = 1) { event.isCancelled = true }
    }

    @Test
    fun `dragging a currency into a smoker is cancelled`() {
        val event = drag(SMOKER, coin(), setOf(0))
        verify(exactly = 1) { event.isCancelled = true }
    }

    @Test
    fun `dragging a currency that reaches one furnace slot from the player's inventory is cancelled`() {
        val event = drag(FURNACE, coin(), setOf(2, 3, 4))
        verify(exactly = 1) { event.isCancelled = true }
    }

    @Test
    fun `dragging a currency only across the player's inventory below a furnace is allowed`() {
        val event = drag(FURNACE, coin(), setOf(3, 4, 5))
        verify(exactly = 0) { event.isCancelled = any() }
        verify(exactly = 0) { player.sendMessage(any<String>()) }
    }

    @Test
    fun `dragging an item that is not a currency across furnace slots is allowed`() {
        val event = drag(FURNACE, mockk(relaxed = true), setOf(0, 1))
        verify(exactly = 0) { event.isCancelled = any() }
    }

    @Test
    fun `dragging a currency in an inventory that is not a furnace is allowed`() {
        val event = drag(CHEST, coin(), setOf(0, 1))
        verify(exactly = 0) { event.isCancelled = any() }
    }

    @Test
    fun `dragging a currency across furnace slots is allowed when disallowSmelting is off`() {
        every { plugin.config.getBoolean("currencies.disallowSmelting") } returns false
        val event = drag(FURNACE, coin(), setOf(0, 1))
        verify(exactly = 0) { event.isCancelled = any() }
    }
}
