package com.dansplugins.currencies.command.currency.balance

import com.dansplugins.currencies.Currencies
import com.dansplugins.currencies.balance.Balance
import com.dansplugins.currencies.currency.Currency
import com.dansplugins.currencies.currency.CurrencyId
import com.dansplugins.currencies.currency.CurrencyStatus.RETIRED
import com.dansplugins.factionsystem.faction.MfFactionId
import com.dansplugins.factionsystem.player.MfPlayerId
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.bukkit.ChatColor.AQUA
import org.bukkit.ChatColor.GRAY
import org.bukkit.ChatColor.RED
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

class CurrencyBalanceCommandTest {

    private val gold = Currency(id = CurrencyId("gold"), factionId = MfFactionId("faction"), name = "Gold", item = mockk(relaxed = true))
    private val silver = Currency(id = CurrencyId("silver"), factionId = MfFactionId("faction"), name = "Silver", item = mockk(relaxed = true), status = RETIRED)
    private val bronze = Currency(id = CurrencyId("bronze"), factionId = MfFactionId("faction"), name = "Bronze", item = mockk(relaxed = true))

    private val plugin = mockk<Currencies>(relaxed = true) {
        every { name } returns "currencies"
    }
    private val bukkitCommand = mockk<Command>(relaxed = true)
    private val balanceCommand = CurrencyBalanceCommand(plugin)

    private fun player(permitted: Boolean) = mockk<Player>(relaxed = true) {
        every { hasPermission("currencies.balance") } returns permitted
        every { uniqueId } returns UUID.randomUUID()
    }

    private fun messagesSentBy(sender: CommandSender): List<String> {
        val messages = mutableListOf<String>()
        every { sender.sendMessage(capture(messages)) } returns Unit
        assertTrue(balanceCommand.onCommand(sender, bukkitCommand, "balance", emptyArray()))
        return messages
    }

    /**
     * The balance report is built on the scheduler's async thread, so the scheduled task is captured
     * and run here rather than only asserting that something was scheduled.
     */
    private fun balanceReportFor(sender: Player, currencies: List<Currency>, balances: List<Balance>): List<String> {
        every { plugin.services.currencyService.currencies } returns currencies
        every { plugin.services.balanceService.getBalances(any()) } returns balances
        val task = slot<Runnable>()
        every { plugin.server.scheduler.runTaskAsynchronously(plugin, capture(task)) } returns mockk(relaxed = true)
        val messages = messagesSentBy(sender)
        task.captured.run()
        return messages
    }

    @Test
    fun `senders without the balance permission are refused`() {
        assertEquals(
            listOf("${RED}You do not have permission to view your balance."),
            messagesSentBy(player(permitted = false))
        )
    }

    @Test
    fun `non-players are refused even when permitted`() {
        val console = mockk<CommandSender>(relaxed = true) {
            every { hasPermission("currencies.balance") } returns true
        }
        assertEquals(
            listOf("${RED}You must be a player to use this command."),
            messagesSentBy(console)
        )
    }

    @Test
    fun `every currency on the server is reported, including ones the player holds none of`() {
        val sender = player(permitted = true)
        val balances = listOf(Balance(MfPlayerId.fromBukkitPlayer(sender), gold.id, balance = 42))
        assertEquals(
            listOf(
                "${AQUA}=== Coinpurse Contents ===",
                "${GRAY}Gold: 42",
                "${GRAY}Bronze: 0"
            ),
            balanceReportFor(sender, listOf(gold, bronze), balances)
        )
    }

    @Test
    fun `retired currencies are reported with a retired marker`() {
        val sender = player(permitted = true)
        val balances = listOf(Balance(MfPlayerId.fromBukkitPlayer(sender), silver.id, balance = 7))
        assertEquals(
            listOf(
                "${AQUA}=== Coinpurse Contents ===",
                "${GRAY}Silver: 7 ${RED}[retired]"
            ),
            balanceReportFor(sender, listOf(silver), balances)
        )
    }

    @Test
    fun `only the header is reported when the server has no currencies`() {
        val sender = player(permitted = true)
        assertEquals(
            listOf("${AQUA}=== Coinpurse Contents ==="),
            balanceReportFor(sender, emptyList(), emptyList())
        )
    }
}
