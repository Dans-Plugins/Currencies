package com.dansplugins.currencies.listener

import com.dansplugins.currencies.Currencies
import com.dansplugins.currencies.currency.Currency
import com.dansplugins.currencies.currency.CurrencyId
import com.dansplugins.factionsystem.faction.MfFactionId
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.inventory.InventoryType.BLAST_FURNACE
import org.bukkit.event.inventory.InventoryType.CHEST
import org.bukkit.event.inventory.InventoryType.FURNACE
import org.bukkit.event.inventory.InventoryType.SMOKER
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class InventoryMoveItemListenerTest {

    private val goldItem = mockk<ItemStack>(relaxed = true)
    private val gold = Currency(id = CurrencyId("gold"), factionId = MfFactionId("faction"), name = "Gold", item = goldItem)

    private val plugin = mockk<Currencies>(relaxed = true)
    private val currencyService = plugin.services.currencyService
    private val listener = InventoryMoveItemListener(plugin)

    @BeforeEach
    fun setUp() {
        every { plugin.config.getBoolean("currencies.disallowSmelting") } returns true
        every { currencyService.currencies } returns listOf(gold)
    }

    private fun move(destinationType: InventoryType, moved: ItemStack): InventoryMoveItemEvent {
        val destination = mockk<Inventory>(relaxed = true) {
            every { type } returns destinationType
        }
        val event = mockk<InventoryMoveItemEvent>(relaxed = true) {
            every { this@mockk.destination } returns destination
            every { item } returns moved
        }
        listener.onInventoryMoveItem(event)
        return event
    }

    private fun coin() = mockk<ItemStack>(relaxed = true).also {
        every { goldItem.isSimilar(it) } returns true
    }

    @Test
    fun `a hopper moving a currency into a furnace is cancelled`() {
        val event = move(FURNACE, coin())
        verify(exactly = 1) { event.isCancelled = true }
    }

    @Test
    fun `a hopper moving a currency into a blast furnace is cancelled`() {
        val event = move(BLAST_FURNACE, coin())
        verify(exactly = 1) { event.isCancelled = true }
    }

    @Test
    fun `a hopper moving a currency into a smoker is cancelled`() {
        val event = move(SMOKER, coin())
        verify(exactly = 1) { event.isCancelled = true }
    }

    @Test
    fun `a hopper moving an item that is not a currency into a furnace is allowed`() {
        val event = move(FURNACE, mockk(relaxed = true))
        verify(exactly = 0) { event.isCancelled = any() }
    }

    @Test
    fun `a hopper moving a currency into an inventory that is not a furnace is allowed`() {
        val event = move(CHEST, coin())
        verify(exactly = 0) { event.isCancelled = any() }
    }

    @Test
    fun `a hopper moving a currency into a furnace is allowed when disallowSmelting is off`() {
        every { plugin.config.getBoolean("currencies.disallowSmelting") } returns false
        val event = move(FURNACE, coin())
        verify(exactly = 0) { event.isCancelled = any() }
    }
}
