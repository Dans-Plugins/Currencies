package com.dansplugins.currencies.listener

import com.dansplugins.currencies.Currencies
import com.dansplugins.currencies.balance.Balance
import com.dansplugins.currencies.coinpurse.CoinpurseInventoryHolder
import com.dansplugins.currencies.currency.Currency
import com.dansplugins.currencies.currency.CurrencyId
import com.dansplugins.factionsystem.faction.MfFactionId
import com.dansplugins.factionsystem.failure.ServiceFailure
import com.dansplugins.factionsystem.failure.ServiceFailureType.GENERAL
import com.dansplugins.factionsystem.player.MfPlayerId
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.bukkit.ChatColor.RED
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class InventoryCloseListenerTest {

    private val goldItem = mockk<ItemStack>(relaxed = true)
    private val silverItem = mockk<ItemStack>(relaxed = true)
    private val gold = Currency(id = CurrencyId("gold"), factionId = MfFactionId("faction"), name = "Gold", item = goldItem)
    private val silver = Currency(id = CurrencyId("silver"), factionId = MfFactionId("faction"), name = "Silver", item = silverItem)

    private val plugin = mockk<Currencies>(relaxed = true) {
        every { name } returns "currencies"
    }
    private val currencyService = plugin.services.currencyService
    private val balanceService = plugin.services.balanceService
    private val listener = InventoryCloseListener(plugin)

    private val world = mockk<World>(relaxed = true)
    private val location = mockk<Location>(relaxed = true)
    private val playerUuid = UUID.randomUUID()
    private val playerId = MfPlayerId(playerUuid.toString())
    private val player = mockk<Player>(relaxed = true) {
        every { uniqueId } returns playerUuid
        every { world } returns this@InventoryCloseListenerTest.world
        every { location } returns this@InventoryCloseListenerTest.location
    }

    private val savedBalances = mutableListOf<Balance>()

    /**
     * Balances are saved on the scheduler's async thread and leftover items are dropped back on the
     * main thread, so every scheduled task is run as soon as it is scheduled rather than only
     * asserting that it was.
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
        every { currencyService.currencies } returns listOf(gold, silver)
        every { balanceService.getBalance(any(), any()) } returns null
        every { balanceService.save(capture(savedBalances)) } answers { Success(firstArg()) }
    }

    private fun coin(currencyItem: ItemStack, amount: Int) = mockk<ItemStack>(relaxed = true) {
        every { this@mockk.amount } returns amount
        every { currencyItem.isSimilar(this@mockk) } returns true
    }

    private fun close(holder: InventoryHolder?, contents: List<ItemStack?>, closer: HumanEntity = player) {
        val inventory = mockk<Inventory>(relaxed = true) {
            every { this@mockk.holder } returns holder
            every { this@mockk.contents } returns contents.toTypedArray()
        }
        val event = mockk<InventoryCloseEvent>(relaxed = true) {
            every { this@mockk.inventory } returns inventory
            every { this@mockk.player } returns closer
        }
        listener.onInventoryClose(event)
    }

    private fun coinpurse() = mockk<CoinpurseInventoryHolder>(relaxed = true)

    @Test
    fun `closing an inventory that is not a coinpurse saves nothing`() {
        close(mockk<InventoryHolder>(relaxed = true), listOf(coin(goldItem, 5)))
        verify(exactly = 0) { balanceService.save(any()) }
        verify(exactly = 0) { plugin.server.scheduler.runTaskAsynchronously(plugin, any<Runnable>()) }
    }

    @Test
    fun `a coinpurse closed by a non-player saves nothing`() {
        close(coinpurse(), listOf(coin(goldItem, 5)), closer = mockk<HumanEntity>(relaxed = true))
        verify(exactly = 0) { balanceService.save(any()) }
    }

    @Test
    fun `each currency's balance is the sum of its stacks in the coinpurse`() {
        close(coinpurse(), listOf(coin(goldItem, 64), null, coin(goldItem, 6), coin(silverItem, 3)))
        assertEquals(
            listOf(
                Balance(playerId, gold.id, balance = 70),
                Balance(playerId, silver.id, balance = 3)
            ),
            savedBalances
        )
    }

    @Test
    fun `a currency absent from the coinpurse is saved with a zero balance`() {
        close(coinpurse(), listOf(coin(goldItem, 12)))
        assertEquals(
            listOf(
                Balance(playerId, gold.id, balance = 12),
                Balance(playerId, silver.id, balance = 0)
            ),
            savedBalances
        )
    }

    @Test
    fun `an existing balance is updated in place, keeping its version`() {
        every { balanceService.getBalance(playerId, gold.id) } returns Balance(playerId, gold.id, version = 4, balance = 100)
        close(coinpurse(), listOf(coin(goldItem, 30)))
        assertEquals(Balance(playerId, gold.id, version = 4, balance = 30), savedBalances.first())
    }

    @Test
    fun `items that are not a currency are dropped at the player's location`() {
        val dirt = mockk<ItemStack>(relaxed = true) {
            every { amount } returns 16
        }
        close(coinpurse(), listOf(coin(goldItem, 2), dirt))
        verify(exactly = 1) { world.dropItem(location, dirt) }
        assertEquals(
            listOf(
                Balance(playerId, gold.id, balance = 2),
                Balance(playerId, silver.id, balance = 0)
            ),
            savedBalances
        )
    }

    @Test
    fun `a failed save is reported to the player and the remaining currencies are still saved`() {
        every { balanceService.save(match { it.currencyId == gold.id }) } returns
            Failure(ServiceFailure(GENERAL, "database unavailable", RuntimeException("database unavailable")))
        val messages = mutableListOf<String>()
        every { player.sendMessage(capture(messages)) } returns Unit
        close(coinpurse(), listOf(coin(goldItem, 1), coin(silverItem, 2)))
        assertEquals(listOf("${RED}Failed to save balance for Gold: database unavailable"), messages)
        verify(exactly = 1) { balanceService.save(Balance(playerId, silver.id, balance = 2)) }
    }
}
