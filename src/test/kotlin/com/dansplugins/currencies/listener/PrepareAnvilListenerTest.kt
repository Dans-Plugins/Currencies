package com.dansplugins.currencies.listener

import com.dansplugins.currencies.Currencies
import com.dansplugins.currencies.currency.Currency
import com.dansplugins.currencies.currency.CurrencyId
import com.dansplugins.factionsystem.faction.MfFactionId
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.bukkit.ChatColor.RED
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.AnvilInventory
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PrepareAnvilListenerTest {

    private val goldItem = mockk<ItemStack>(relaxed = true)
    private val gold = Currency(id = CurrencyId("gold"), factionId = MfFactionId("faction"), name = "Gold", item = goldItem)

    private val plugin = mockk<Currencies>(relaxed = true)
    private val currencyService = plugin.services.currencyService
    private val listener = PrepareAnvilListener(plugin)

    private val player = mockk<Player>(relaxed = true)

    @BeforeEach
    fun setUp() {
        every { plugin.config.getBoolean("currencies.disallowAnvilUsage") } returns true
        every { currencyService.currencies } returns listOf(gold)
    }

    private fun prepare(contents: List<ItemStack?>, viewers: List<HumanEntity> = listOf(player)): PrepareAnvilEvent {
        val inventory = mockk<AnvilInventory>(relaxed = true) {
            every { this@mockk.contents } returns contents.toTypedArray()
        }
        val event = mockk<PrepareAnvilEvent>(relaxed = true) {
            every { this@mockk.inventory } returns inventory
            every { this@mockk.viewers } returns viewers
        }
        listener.onPrepareAnvil(event)
        return event
    }

    private fun coin() = mockk<ItemStack>(relaxed = true).also {
        every { goldItem.isSimilar(it) } returns true
    }

    @Test
    fun `a currency in the anvil clears the result and the player is told why`() {
        val event = prepare(listOf(mockk(relaxed = true), coin()))
        verify(exactly = 1) { event.result = null }
        verify(exactly = 1) { player.sendMessage("${RED}You cannot use currencies when renaming or repairing.") }
    }

    @Test
    fun `two currencies in the anvil are reported once`() {
        prepare(listOf(coin(), coin()))
        verify(exactly = 1) { player.sendMessage(any<String>()) }
    }

    @Test
    fun `an anvil holding no currency is left alone`() {
        val event = prepare(listOf(mockk(relaxed = true), null))
        verify(exactly = 0) { event.result = any() }
        verify(exactly = 0) { player.sendMessage(any<String>()) }
    }

    @Test
    fun `a currency in the anvil is allowed when disallowAnvilUsage is off`() {
        every { plugin.config.getBoolean("currencies.disallowAnvilUsage") } returns false
        val event = prepare(listOf(coin()))
        verify(exactly = 0) { event.result = any() }
    }

    @Test
    fun `an anvil with no player viewing it is left alone`() {
        val event = prepare(listOf(coin()), viewers = emptyList())
        verify(exactly = 0) { event.result = any() }
    }
}
