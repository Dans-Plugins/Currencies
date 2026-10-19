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
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.inventory.CraftingInventory
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PrepareItemCraftListenerTest {

    private val goldItem = mockk<ItemStack>(relaxed = true)
    private val gold = Currency(id = CurrencyId("gold"), factionId = MfFactionId("faction"), name = "Gold", item = goldItem)

    private val plugin = mockk<Currencies>(relaxed = true)
    private val currencyService = plugin.services.currencyService
    private val listener = PrepareItemCraftListener(plugin)

    private val player = mockk<Player>(relaxed = true)
    private val inventory = mockk<CraftingInventory>(relaxed = true)

    @BeforeEach
    fun setUp() {
        every { plugin.config.getBoolean("currencies.disallowCrafting") } returns true
        every { currencyService.currencies } returns listOf(gold)
    }

    private fun prepare(contents: List<ItemStack?>, viewers: List<HumanEntity> = listOf(player)) {
        every { inventory.contents } returns contents.toTypedArray()
        val event = mockk<PrepareItemCraftEvent>(relaxed = true) {
            every { this@mockk.inventory } returns this@PrepareItemCraftListenerTest.inventory
            every { this@mockk.viewers } returns viewers
        }
        listener.onPrepareItemCraft(event)
    }

    private fun coin() = mockk<ItemStack>(relaxed = true).also {
        every { goldItem.isSimilar(it) } returns true
    }

    @Test
    fun `a currency in the crafting grid clears the result and the player is told why`() {
        prepare(listOf(mockk(relaxed = true), coin()))
        verify(exactly = 1) { inventory.result = null }
        verify(exactly = 1) { player.sendMessage("${RED}You cannot use currencies when crafting.") }
    }

    @Test
    fun `two currencies in the crafting grid are reported once`() {
        prepare(listOf(coin(), coin()))
        verify(exactly = 1) { player.sendMessage(any<String>()) }
    }

    @Test
    fun `a crafting grid holding no currency is left alone`() {
        prepare(listOf(mockk(relaxed = true), null))
        verify(exactly = 0) { inventory.result = any() }
        verify(exactly = 0) { player.sendMessage(any<String>()) }
    }

    @Test
    fun `a currency in the crafting grid is allowed when disallowCrafting is off`() {
        every { plugin.config.getBoolean("currencies.disallowCrafting") } returns false
        prepare(listOf(coin()))
        verify(exactly = 0) { inventory.result = any() }
    }

    @Test
    fun `a crafting grid with no player viewing it is left alone`() {
        prepare(listOf(coin()), viewers = emptyList())
        verify(exactly = 0) { inventory.result = any() }
    }
}
