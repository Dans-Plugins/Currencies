package com.dansplugins.currencies.balance

import com.dansplugins.currencies.Currencies
import com.dansplugins.currencies.currency.CurrencyId
import com.dansplugins.factionsystem.failure.OptimisticLockingFailureException
import com.dansplugins.factionsystem.failure.ServiceFailureType.CONFLICT
import com.dansplugins.factionsystem.failure.ServiceFailureType.GENERAL
import com.dansplugins.factionsystem.player.MfPlayerId
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BalanceServiceTest {

    private val plugin = mockk<Currencies>(relaxed = true)

    private val alice = MfPlayerId("alice")
    private val bob = MfPlayerId("bob")
    private val gold = CurrencyId("gold")
    private val silver = CurrencyId("silver")

    /**
     * An in-memory stand-in for the jOOQ repository. Each upsert bumps the version, as the real
     * repository's optimistic locking does, so the service's returned value is distinguishable from
     * the value passed in. [failWith] makes the next upserts throw instead.
     */
    private class FakeBalanceRepository(vararg initial: Balance) : BalanceRepository {
        val stored = initial.associateBy { it.playerId to it.currencyId }.toMutableMap()
        var failWith: Exception? = null

        override fun getBalance(playerId: MfPlayerId, currencyId: CurrencyId) = stored[playerId to currencyId]

        override fun getBalances(playerId: MfPlayerId) = stored.values.filter { it.playerId == playerId }

        override fun upsert(balance: Balance): Balance {
            failWith?.let { throw it }
            val saved = balance.copy(version = balance.version + 1)
            stored[balance.playerId to balance.currencyId] = saved
            return saved
        }
    }

    @Test
    fun `a balance is looked up by player and currency`() {
        val aliceGold = Balance(alice, gold, balance = 10)
        val service = BalanceService(plugin, FakeBalanceRepository(aliceGold, Balance(bob, gold, balance = 3)))

        assertEquals(aliceGold, service.getBalance(alice, gold))
        assertNull(service.getBalance(alice, silver))
    }

    @Test
    fun `a player's balances are listed across currencies`() {
        val aliceGold = Balance(alice, gold, balance = 10)
        val aliceSilver = Balance(alice, silver, balance = 4)
        val service = BalanceService(plugin, FakeBalanceRepository(aliceGold, aliceSilver, Balance(bob, gold, balance = 3)))

        assertEquals(setOf(aliceGold, aliceSilver), service.getBalances(alice).toSet())
        assertTrue(service.getBalances(MfPlayerId("carol")).isEmpty())
    }

    @Test
    fun `balance reads are not cached`() {
        val repo = FakeBalanceRepository(Balance(alice, gold, balance = 10))
        val service = BalanceService(plugin, repo)
        service.getBalance(alice, gold)

        repo.stored[alice to gold] = Balance(alice, gold, version = 5, balance = 99)

        assertEquals(99, service.getBalance(alice, gold)?.balance)
        assertEquals(listOf(99), service.getBalances(alice).map { it.balance })
    }

    @Test
    fun `a saved balance is written to the repository and the repository's result is returned`() {
        val repo = FakeBalanceRepository(Balance(alice, gold, balance = 10))
        val service = BalanceService(plugin, repo)
        val updated = Balance(alice, gold, version = 0, balance = 25)

        val result = service.save(updated)

        val saved = updated.copy(version = 1)
        assertEquals(Success(saved), result)
        assertEquals(saved, repo.stored[alice to gold])
    }

    @Test
    fun `an optimistic locking failure is reported as a conflict and the stored balance is untouched`() {
        val original = Balance(alice, gold, version = 2, balance = 10)
        val repo = FakeBalanceRepository(original)
        val service = BalanceService(plugin, repo)
        val exception = OptimisticLockingFailureException("Invalid version: 1")
        repo.failWith = exception

        val result = service.save(Balance(alice, gold, version = 1, balance = 50))

        val failure = (result as Failure).reason
        assertEquals(CONFLICT, failure.type)
        assertEquals("Service error: Invalid version: 1", failure.message)
        assertSame(exception, failure.cause)
        assertEquals(original, repo.stored[alice to gold])
    }

    @Test
    fun `any other repository failure is reported as a general failure`() {
        val repo = FakeBalanceRepository()
        val service = BalanceService(plugin, repo)
        val exception = IllegalStateException("database unavailable")
        repo.failWith = exception

        val result = service.save(Balance(alice, gold, balance = 5))

        val failure = (result as Failure).reason
        assertEquals(GENERAL, failure.type)
        assertEquals("Service error: database unavailable", failure.message)
        assertSame(exception, failure.cause)
        assertNull(repo.stored[alice to gold])
    }
}
