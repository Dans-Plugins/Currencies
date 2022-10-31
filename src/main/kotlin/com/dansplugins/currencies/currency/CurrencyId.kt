package com.dansplugins.currencies.currency

import java.util.*

@JvmInline
value class CurrencyId(val value: String) {
    companion object {
        fun generate() = CurrencyId(UUID.randomUUID().toString())
    }
}