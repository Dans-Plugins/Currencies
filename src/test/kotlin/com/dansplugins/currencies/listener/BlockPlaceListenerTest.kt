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
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class BlockPlaceListenerTest {

    private val goldItem = mockk<ItemStack>(relaxed = true)
    private val gold = Currency(id = CurrencyId("gold"), factionId = MfFactionId("faction"), name = "Gold", item = goldItem)

    private val plugin = mockk<Currencies>(relaxed = true)
    private val currencyService = plugin.services.currencyService
    private val listener = BlockPlaceListener(plugin)

    private val player = mockk<Player>(relaxed = true)

    @BeforeEach
    fun setUp() {
        every { plugin.config.getBoolean("currencies.disallowPlacement") } returns true
        every { currencyService.currencies } returns listOf(gold)
    }

    private fun place(item: ItemStack): BlockPlaceEvent {
        val event = mockk<BlockPlaceEvent>(relaxed = true) {
            every { itemInHand } returns item
            every { this@mockk.player } returns this@BlockPlaceListenerTest.player
        }
        listener.onBlockPlace(event)
        return event
    }

    private fun coin() = mockk<ItemStack>(relaxed = true).also {
        every { goldItem.isSimilar(it) } returns true
    }

    @Test
    fun `placing a currency is cancelled and the player is told why`() {
        val event = place(coin())
        verify(exactly = 1) { event.isCancelled = true }
        verify(exactly = 1) { player.sendMessage("${RED}You cannot place currencies.") }
    }

    @Test
    fun `placing an item that is not a currency is allowed`() {
        val event = place(mockk(relaxed = true))
        verify(exactly = 0) { event.isCancelled = any() }
        verify(exactly = 0) { player.sendMessage(any<String>()) }
    }

    @Test
    fun `placing a currency is allowed when disallowPlacement is off`() {
        every { plugin.config.getBoolean("currencies.disallowPlacement") } returns false
        val event = place(coin())
        verify(exactly = 0) { event.isCancelled = any() }
        verify(exactly = 0) { currencyService.currencies }
    }
}
