package com.dansplugins.currencies.currency

import com.dansplugins.currencies.item.toByteArray
import com.dansplugins.currencies.item.toItemStack
import com.dansplugins.currencies.jooq.Tables.CURRENCIES_CURRENCY
import com.dansplugins.currencies.jooq.tables.records.CurrenciesCurrencyRecord
import com.dansplugins.factionsystem.faction.MfFactionId
import com.dansplugins.factionsystem.failure.OptimisticLockingFailureException
import org.jooq.DSLContext

class JooqCurrencyRepository(private val dsl: DSLContext) : CurrencyRepository {
    override fun getCurrencies(): List<Currency> =
        dsl.selectFrom(CURRENCIES_CURRENCY)
            .fetch()
            .map { it.toDomain() }

    override fun getCurrency(id: CurrencyId): Currency? =
        dsl.selectFrom(CURRENCIES_CURRENCY)
            .where(CURRENCIES_CURRENCY.ID.eq(id.value))
            .fetchOne()
            ?.toDomain()

    override fun upsert(currency: Currency): Currency {
        val rowCount = dsl.insertInto(CURRENCIES_CURRENCY)
            .set(CURRENCIES_CURRENCY.ID, currency.id.value)
            .set(CURRENCIES_CURRENCY.VERSION, 1)
            .set(CURRENCIES_CURRENCY.FACTION_ID, currency.factionId.value)
            .set(CURRENCIES_CURRENCY.NAME, currency.name)
            .set(CURRENCIES_CURRENCY.DESCRIPTION, currency.description)
            .set(CURRENCIES_CURRENCY.ITEM, currency.item.toByteArray())
            .set(CURRENCIES_CURRENCY.AMOUNT, currency.amount)
            .set(CURRENCIES_CURRENCY.STATUS, currency.status.name)
            .set(CURRENCIES_CURRENCY.LEGACY_ID, currency.legacyId)
            .onConflict(CURRENCIES_CURRENCY.ID).doUpdate()
            .set(CURRENCIES_CURRENCY.FACTION_ID, currency.factionId.value)
            .set(CURRENCIES_CURRENCY.NAME, currency.name)
            .set(CURRENCIES_CURRENCY.DESCRIPTION, currency.description)
            .set(CURRENCIES_CURRENCY.ITEM, currency.item.toByteArray())
            .set(CURRENCIES_CURRENCY.AMOUNT, currency.amount)
            .set(CURRENCIES_CURRENCY.STATUS, currency.status.name)
            .set(CURRENCIES_CURRENCY.LEGACY_ID, currency.legacyId)
            .set(CURRENCIES_CURRENCY.VERSION, currency.version + 1)
            .where(CURRENCIES_CURRENCY.ID.eq(currency.id.value))
            .and(CURRENCIES_CURRENCY.VERSION.eq(currency.version))
            .execute()
        if (rowCount == 0) throw OptimisticLockingFailureException("Invalid version: ${currency.version}")
        return getCurrency(currency.id).let(::requireNotNull)
    }

    private fun CurrenciesCurrencyRecord.toDomain() = Currency(
        id.let(::CurrencyId),
        version,
        factionId.let(::MfFactionId),
        name,
        description,
        item.toItemStack(),
        amount,
        status.let(CurrencyStatus::valueOf),
        legacyId
    )
}