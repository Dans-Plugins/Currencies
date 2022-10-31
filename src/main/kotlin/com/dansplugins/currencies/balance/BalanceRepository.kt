package com.dansplugins.currencies.balance

import com.dansplugins.currencies.currency.CurrencyId
import com.dansplugins.factionsystem.player.MfPlayerId

interface BalanceRepository {

    fun getBalance(playerId: MfPlayerId, currencyId: CurrencyId): Balance?
    fun getBalances(playerId: MfPlayerId): List<Balance>
    fun upsert(balance: Balance): Balance
}