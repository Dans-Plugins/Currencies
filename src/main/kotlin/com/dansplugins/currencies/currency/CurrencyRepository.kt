package com.dansplugins.currencies.currency

interface CurrencyRepository {
    fun getCurrencies(): List<Currency>
    fun getCurrency(id: CurrencyId): Currency?
    fun upsert(currency: Currency): Currency
}