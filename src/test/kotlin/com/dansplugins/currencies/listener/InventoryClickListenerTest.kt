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
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.ClickType.LEFT
import org.bukkit.event.inventory.ClickType.NUMBER_KEY
import org.bukkit.event.inventory.ClickType.SWAP_OFFHAND
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.inventory.InventoryType.BLAST_FURNACE
import org.bukkit.event.inventory.InventoryType.CHEST
import org.bukkit.event.inventory.InventoryType.FURNACE
import org.bukkit.event.inventory.InventoryType.PLAYER
import org.bukkit.event.inventory.InventoryType.SMOKER
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class InventoryClickListenerTest {

    private val goldItem = mockk<ItemStack>(relaxed = true)
    private val gold = Currency(id = CurrencyId("gold"), factionId = MfFactionId("faction"), name = "Gold", item = goldItem)

    private val plugin = mockk<Currencies>(relaxed = true)
    private val currencyService = plugin.services.currencyService
    private val listener = InventoryClickListener(plugin)

    private val playerInventory = mockk<PlayerInventory>(relaxed = true)
    private val player = mockk<Player>(relaxed = true) {
        every { inventory } returns playerInventory
    }

    @BeforeEach
    fun setUp() {
        every { plugin.config.getBoolean("currencies.disallowSmelting") } returns true
        every { currencyService.currencies } returns listOf(gold)
    }

    private fun click(
        type: InventoryType,
        clicked: ItemStack?,
        cursor: ItemStack? = null,
        clickedType: InventoryType = type,
        clickType: ClickType = LEFT,
        hotbarButton: Int = -1
    ): InventoryClickEvent {
        val inventory = mockk<Inventory>(relaxed = true) {
            every { this@mockk.type } returns type
        }
        val clickedInventory = mockk<Inventory>(relaxed = true) {
            every { this@mockk.type } returns clickedType
        }
        val event = mockk<InventoryClickEvent>(relaxed = true) {
            every { this@mockk.inventory } returns inventory
            every { this@mockk.clickedInventory } returns clickedInventory
            every { currentItem } returns clicked
            every { this@mockk.cursor } returns cursor
            every { click } returns clickType
            every { this@mockk.hotbarButton } returns hotbarButton
            every { whoClicked } returns player
        }
        listener.onInventoryClick(event)
        return event
    }

    private fun coin() = mockk<ItemStack>(relaxed = true).also {
        every { goldItem.isSimilar(it) } returns true
    }

    @Test
    fun `clicking a currency in a furnace is cancelled and the player is told why`() {
        val event = click(FURNACE, coin())
        verify(exactly = 1) { event.isCancelled = true }
        verify(exactly = 1) { player.sendMessage("${RED}You can't smelt a currency.") }
    }

    @Test
    fun `clicking a currency in a blast furnace is cancelled`() {
        val event = click(BLAST_FURNACE, coin())
        verify(exactly = 1) { event.isCancelled = true }
    }

    @Test
    fun `clicking an item that is not a currency in a furnace is allowed`() {
        val event = click(FURNACE, mockk(relaxed = true))
        verify(exactly = 0) { event.isCancelled = any() }
        verify(exactly = 0) { player.sendMessage(any<String>()) }
    }

    @Test
    fun `clicking a currency in an inventory that is not a furnace is allowed`() {
        val event = click(CHEST, coin())
        verify(exactly = 0) { event.isCancelled = any() }
    }

    @Test
    fun `clicking a currency in a furnace is allowed when disallowSmelting is off`() {
        every { plugin.config.getBoolean("currencies.disallowSmelting") } returns false
        val event = click(FURNACE, coin())
        verify(exactly = 0) { event.isCancelled = any() }
    }

    @Test
    fun `clicking a currency in a smoker is cancelled`() {
        val event = click(SMOKER, coin())
        verify(exactly = 1) { event.isCancelled = true }
    }

    @Test
    fun `a currency on the cursor clicked into an empty furnace slot is cancelled`() {
        val event = click(FURNACE, clicked = null, cursor = coin())
        verify(exactly = 1) { event.isCancelled = true }
        verify(exactly = 1) { player.sendMessage("${RED}You can't smelt a currency.") }
    }

    @Test
    fun `a currency on the cursor clicked onto an item that is not a currency in a furnace slot is cancelled`() {
        val event = click(FURNACE, clicked = mockk(relaxed = true), cursor = coin())
        verify(exactly = 1) { event.isCancelled = true }
    }

    @Test
    fun `a currency on the cursor clicked into the player's own inventory below a furnace is allowed`() {
        val event = click(FURNACE, clicked = null, cursor = coin(), clickedType = PLAYER)
        verify(exactly = 0) { event.isCancelled = any() }
    }

    @Test
    fun `a currency swapped from the hotbar into a furnace slot with a number key is cancelled`() {
        every { playerInventory.getItem(3) } returns coin()
        val event = click(FURNACE, clicked = null, clickType = NUMBER_KEY, hotbarButton = 3)
        verify(exactly = 1) { event.isCancelled = true }
    }

    @Test
    fun `a currency swapped from the off hand into a furnace slot is cancelled`() {
        every { playerInventory.itemInOffHand } returns coin()
        val event = click(FURNACE, clicked = null, clickType = SWAP_OFFHAND)
        verify(exactly = 1) { event.isCancelled = true }
    }

    @Test
    fun `an item that is not a currency on the cursor clicked into a furnace slot is allowed`() {
        val event = click(FURNACE, clicked = null, cursor = mockk(relaxed = true))
        verify(exactly = 0) { event.isCancelled = any() }
    }
}
