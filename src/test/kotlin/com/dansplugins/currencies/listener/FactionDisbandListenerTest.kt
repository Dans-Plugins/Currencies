package com.dansplugins.currencies.listener

import com.dansplugins.currencies.Currencies
import com.dansplugins.currencies.currency.Currency
import com.dansplugins.currencies.currency.CurrencyId
import com.dansplugins.currencies.currency.CurrencyStatus.RETIRED
import com.dansplugins.factionsystem.event.faction.FactionDisbandEvent
import com.dansplugins.factionsystem.faction.MfFactionId
import com.dansplugins.factionsystem.failure.ServiceFailure
import com.dansplugins.factionsystem.failure.ServiceFailureType.GENERAL
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.logging.Level.SEVERE

class FactionDisbandListenerTest {

    private val factionId = MfFactionId("faction")
    private val gold = Currency(id = CurrencyId("gold"), factionId = factionId, name = "Gold", item = mockk(relaxed = true))
    private val silver = Currency(id = CurrencyId("silver"), factionId = factionId, name = "Silver", item = mockk(relaxed = true))
    private val copper = Currency(id = CurrencyId("copper"), factionId = factionId, name = "Copper", item = mockk(relaxed = true), status = RETIRED)

    private val plugin = mockk<Currencies>(relaxed = true)
    private val currencyService = plugin.services.currencyService
    private val listener = FactionDisbandListener(plugin)

    private val savedCurrencies = mutableListOf<Currency>()

    @BeforeEach
    fun setUp() {
        every { currencyService.getCurrencies(any<MfFactionId>()) } returns emptyList()
        every { currencyService.save(capture(savedCurrencies)) } answers { Success(firstArg()) }
    }

    private fun disband(id: MfFactionId = factionId) {
        listener.onFactionDisband(mockk<FactionDisbandEvent>(relaxed = true) {
            every { factionId } returns id
        })
    }

    @Test
    fun `every active currency of the disbanded faction is retired`() {
        every { currencyService.getCurrencies(factionId) } returns listOf(gold, silver)
        disband()
        assertEquals(listOf(gold.copy(status = RETIRED), silver.copy(status = RETIRED)), savedCurrencies)
    }

    @Test
    fun `a currency that is already retired is not saved again`() {
        every { currencyService.getCurrencies(factionId) } returns listOf(gold, copper)
        disband()
        assertEquals(listOf(gold.copy(status = RETIRED)), savedCurrencies)
    }

    @Test
    fun `retiring keeps everything but the status`() {
        val minted = gold.copy(version = 3, description = "Struck in the capital", amount = 120)
        every { currencyService.getCurrencies(factionId) } returns listOf(minted)
        disband()
        assertEquals(listOf(minted.copy(status = RETIRED)), savedCurrencies)
    }

    @Test
    fun `currencies of other factions are left alone`() {
        every { currencyService.getCurrencies(factionId) } returns listOf(gold)
        disband(MfFactionId("another faction"))
        verify(exactly = 0) { currencyService.save(any()) }
    }

    @Test
    fun `a failed save is logged and the remaining currencies are still retired`() {
        val failure = RuntimeException("database unavailable")
        every { currencyService.getCurrencies(factionId) } returns listOf(gold, silver)
        every { currencyService.save(match { it.id == gold.id }) } returns
            Failure(ServiceFailure(GENERAL, "database unavailable", failure))
        disband()
        verify(exactly = 1) { plugin.logger.log(SEVERE, "Failed to save currency: database unavailable", failure) }
        verify(exactly = 1) { currencyService.save(silver.copy(status = RETIRED)) }
    }
}
