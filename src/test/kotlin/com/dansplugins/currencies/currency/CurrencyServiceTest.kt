package com.dansplugins.currencies.currency

import com.dansplugins.currencies.Currencies
import com.dansplugins.currencies.currency.CurrencyStatus.ACTIVE
import com.dansplugins.currencies.currency.CurrencyStatus.RETIRED
import com.dansplugins.factionsystem.faction.MfFactionId
import com.dansplugins.factionsystem.failure.OptimisticLockingFailureException
import com.dansplugins.factionsystem.failure.ServiceFailureType.CONFLICT
import com.dansplugins.factionsystem.failure.ServiceFailureType.GENERAL
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import io.mockk.mockk
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CurrencyServiceTest {

    private val plugin = mockk<Currencies>(relaxed = true)

    private val gold = Currency(id = CurrencyId("gold"), factionId = MfFactionId("faction"), name = "Gold", item = mockk<ItemStack>())
    private val silver = Currency(id = CurrencyId("silver"), factionId = MfFactionId("faction"), name = "Silver", item = mockk<ItemStack>(), status = RETIRED)
    private val copper = Currency(id = CurrencyId("copper"), factionId = MfFactionId("other-faction"), name = "Copper", item = mockk<ItemStack>())

    /**
     * An in-memory stand-in for the jOOQ repository. Each upsert bumps the version, as the real
     * repository's optimistic locking does, so the service's returned value is distinguishable from
     * the value passed in. [failWith] makes the next upserts throw instead.
     */
    private class FakeCurrencyRepository(vararg initial: Currency) : CurrencyRepository {
        val stored = initial.associateBy { it.id }.toMutableMap()
        var failWith: Exception? = null
        var getCurrenciesCalls = 0

        override fun getCurrencies(): List<Currency> {
            getCurrenciesCalls++
            return stored.values.toList()
        }

        override fun getCurrency(id: CurrencyId) = stored[id]

        override fun upsert(currency: Currency): Currency {
            failWith?.let { throw it }
            val saved = currency.copy(version = currency.version + 1)
            stored[currency.id] = saved
            return saved
        }
    }

    @Test
    fun `currencies are loaded from the repository once, when the service is created`() {
        val repo = FakeCurrencyRepository(gold, silver)
        val service = CurrencyService(plugin, repo)

        assertEquals(setOf(gold, silver), service.currencies.toSet())
        service.currencies
        service.getCurrency(CurrencyId("gold"))
        assertEquals(1, repo.getCurrenciesCalls)
    }

    @Test
    fun `currencies added to the repository after creation are not seen by the service`() {
        val repo = FakeCurrencyRepository(gold)
        val service = CurrencyService(plugin, repo)
        repo.stored[silver.id] = silver

        assertNull(service.getCurrency(silver.id))
        assertEquals(listOf(gold), service.currencies)
    }

    @Test
    fun `a currency is found by its id`() {
        val service = CurrencyService(plugin, FakeCurrencyRepository(gold, silver))

        assertEquals(silver, service.getCurrency(CurrencyId("silver")))
        assertNull(service.getCurrency(CurrencyId("platinum")))
    }

    @Test
    fun `a currency is found by its name regardless of case`() {
        val service = CurrencyService(plugin, FakeCurrencyRepository(gold, silver))

        assertEquals(gold, service.getCurrency("gold"))
        assertEquals(gold, service.getCurrency("GOLD"))
        assertNull(service.getCurrency("Platinum"))
    }

    @Test
    fun `a name shared by two currencies resolves to neither`() {
        val goldAgain = gold.copy(id = CurrencyId("gold-2"), name = "gold")
        val service = CurrencyService(plugin, FakeCurrencyRepository(gold, goldAgain))

        assertNull(service.getCurrency("Gold"))
    }

    @Test
    fun `currencies are listed by faction`() {
        val service = CurrencyService(plugin, FakeCurrencyRepository(gold, silver, copper))

        assertEquals(setOf(gold, silver), service.getCurrencies(MfFactionId("faction")).toSet())
        assertEquals(listOf(copper), service.getCurrencies(MfFactionId("other-faction")))
        assertTrue(service.getCurrencies(MfFactionId("no-faction")).isEmpty())
    }

    @Test
    fun `currencies are listed by status`() {
        val service = CurrencyService(plugin, FakeCurrencyRepository(gold, silver, copper))

        assertEquals(setOf(gold, copper), service.getCurrencies(ACTIVE).toSet())
        assertEquals(listOf(silver), service.getCurrencies(RETIRED))
    }

    @Test
    fun `a saved currency is written to the repository and the cache holds the repository's result`() {
        val repo = FakeCurrencyRepository(gold)
        val service = CurrencyService(plugin, repo)
        val renamed = gold.copy(name = "Shiny Gold")

        val result = service.save(renamed)

        val saved = renamed.copy(version = 1)
        assertEquals(Success(saved), result)
        assertEquals(saved, repo.stored[gold.id])
        assertEquals(saved, service.getCurrency(gold.id))
        assertEquals(saved, service.getCurrency("shiny gold"))
        assertNull(service.getCurrency("Gold"))
    }

    @Test
    fun `a newly saved currency is added to the cache`() {
        val service = CurrencyService(plugin, FakeCurrencyRepository(gold))

        service.save(copper)

        assertEquals(copper.copy(version = 1), service.getCurrency(copper.id))
        assertEquals(2, service.currencies.size)
    }

    @Test
    fun `an optimistic locking failure is reported as a conflict and leaves the cache unchanged`() {
        val repo = FakeCurrencyRepository(gold)
        val service = CurrencyService(plugin, repo)
        val exception = OptimisticLockingFailureException("Invalid version: 0")
        repo.failWith = exception

        val result = service.save(gold.copy(name = "Shiny Gold"))

        val failure = (result as Failure).reason
        assertEquals(CONFLICT, failure.type)
        assertEquals("Service error: Invalid version: 0", failure.message)
        assertSame(exception, failure.cause)
        assertEquals(gold, service.getCurrency(gold.id))
    }

    @Test
    fun `any other repository failure is reported as a general failure and leaves the cache unchanged`() {
        val repo = FakeCurrencyRepository(gold)
        val service = CurrencyService(plugin, repo)
        val exception = IllegalStateException("database unavailable")
        repo.failWith = exception

        val result = service.save(copper)

        val failure = (result as Failure).reason
        assertEquals(GENERAL, failure.type)
        assertEquals("Service error: database unavailable", failure.message)
        assertSame(exception, failure.cause)
        assertNull(service.getCurrency(copper.id))
    }
}
