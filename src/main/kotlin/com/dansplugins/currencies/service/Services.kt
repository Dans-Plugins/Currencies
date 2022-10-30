package com.dansplugins.currencies.service

import com.dansplugins.currencies.balance.BalanceService
import com.dansplugins.currencies.currency.CurrencyService

class Services(
    val currencyService: CurrencyService,
    val balanceService: BalanceService
)