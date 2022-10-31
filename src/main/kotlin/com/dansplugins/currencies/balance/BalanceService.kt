package com.dansplugins.currencies.balance

import com.dansplugins.currencies.Currencies
import com.dansplugins.currencies.currency.CurrencyId
import com.dansplugins.factionsystem.failure.OptimisticLockingFailureException
import com.dansplugins.factionsystem.failure.ServiceFailure
import com.dansplugins.factionsystem.failure.ServiceFailureType
import com.dansplugins.factionsystem.player.MfPlayerId
import dev.forkhandles.result4k.mapFailure
import dev.forkhandles.result4k.resultFrom

class BalanceService(private val plugin: Currencies, private val repo: BalanceRepository) {

    fun getBalance(playerId: MfPlayerId, currencyId: CurrencyId) =
        repo.getBalance(playerId, currencyId)

    fun getBalances(playerId: MfPlayerId) =
        repo.getBalances(playerId)

    fun save(balance: Balance) = resultFrom {
        repo.upsert(balance)
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