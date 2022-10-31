package com.dansplugins.currencies.currency

import com.dansplugins.currencies.Currencies
import com.dansplugins.factionsystem.faction.MfFactionId
import com.dansplugins.factionsystem.failure.OptimisticLockingFailureException
import com.dansplugins.factionsystem.failure.ServiceFailure
import com.dansplugins.factionsystem.failure.ServiceFailureType
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.mapFailure
import dev.forkhandles.result4k.resultFrom
import java.util.concurrent.ConcurrentHashMap

class CurrencyService(
    private val plugin: Currencies,
    private val repo: CurrencyRepository
) {

    private val currenciesById: MutableMap<CurrencyId, Currency> = ConcurrentHashMap()
    val currencies: List<Currency>
        get() = currenciesById.values.toList()

    init {
        plugin.logger.info("Loading currencies...")
        val startTime = System.currentTimeMillis()
        currenciesById.putAll(repo.getCurrencies().associateBy { it.id })
        plugin.logger.info("${currenciesById.size} currencies loaded (${System.currentTimeMillis() - startTime}ms)")
    }

    fun getCurrency(id: CurrencyId): Currency? = currenciesById[id]

    fun getCurrency(name: String): Currency? = currencies.singleOrNull { it.name.equals(name, ignoreCase = true) }

    fun getCurrencies(factionId: MfFactionId): List<Currency> = currencies.filter { it.factionId == factionId }

    fun getCurrencies(status: CurrencyStatus): List<Currency> = currencies.filter { it.status == status }

    fun save(currency: Currency): Result4k<Currency, ServiceFailure> = resultFrom {
        val result = repo.upsert(currency)
        currenciesById[currency.id] = result
        return@resultFrom result
    }.mapFailure { exception ->
        ServiceFailure(exception.toServiceFailureType(), "Service error: ${exception.message}", exception)
    }

    private fun Exception.toServiceFailureType(): ServiceFailureType {
        return when (this) {
            is OptimisticLockingFailureException -> ServiceFailureType.CONFLICT
            else -> ServiceFailureType.GENERAL
        }
    }

}