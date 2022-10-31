package com.dansplugins.currencies.balance

import com.dansplugins.currencies.currency.CurrencyId
import com.dansplugins.factionsystem.player.MfPlayerId

data class Balance(
    val playerId: MfPlayerId,
    val currencyId: CurrencyId,
    val version: Int = 0,
    val balance: Int
)