package com.dansplugins.currencies.balance

import com.dansplugins.currencies.currency.CurrencyId
import com.dansplugins.currencies.jooq.Tables.CURRENCIES_BALANCE
import com.dansplugins.currencies.jooq.tables.records.CurrenciesBalanceRecord
import com.dansplugins.factionsystem.failure.OptimisticLockingFailureException
import com.dansplugins.factionsystem.player.MfPlayerId
import org.jooq.DSLContext

class JooqBalanceRepository(private val dsl: DSLContext) : BalanceRepository {
    override fun getBalance(playerId: MfPlayerId, currencyId: CurrencyId): Balance? =
        dsl.selectFrom(CURRENCIES_BALANCE)
            .where(CURRENCIES_BALANCE.PLAYER_ID.eq(playerId.value))
            .and(CURRENCIES_BALANCE.CURRENCY_ID.eq(currencyId.value))
            .fetchOne()
            ?.toDomain()

    override fun getBalances(playerId: MfPlayerId): List<Balance> =
        dsl.selectFrom(CURRENCIES_BALANCE)
            .where(CURRENCIES_BALANCE.PLAYER_ID.eq(playerId.value))
            .fetch()
            .map { it.toDomain() }

    override fun upsert(balance: Balance): Balance {
        val rowCount = dsl.insertInto(CURRENCIES_BALANCE)
            .set(CURRENCIES_BALANCE.PLAYER_ID, balance.playerId.value)
            .set(CURRENCIES_BALANCE.CURRENCY_ID, balance.currencyId.value)
            .set(CURRENCIES_BALANCE.VERSION, 1)
            .set(CURRENCIES_BALANCE.BALANCE, balance.balance)
            .onConflict(CURRENCIES_BALANCE.PLAYER_ID, CURRENCIES_BALANCE.CURRENCY_ID).doUpdate()
            .set(CURRENCIES_BALANCE.BALANCE, balance.balance)
            .set(CURRENCIES_BALANCE.VERSION, balance.version + 1)
            .where(CURRENCIES_BALANCE.PLAYER_ID.eq(balance.playerId.value))
            .and(CURRENCIES_BALANCE.CURRENCY_ID.eq(balance.currencyId.value))
            .and(CURRENCIES_BALANCE.VERSION.eq(balance.version))
            .execute()
        if (rowCount == 0) throw OptimisticLockingFailureException("Invalid version: ${balance.version}")
        return getBalance(balance.playerId, balance.currencyId).let(::requireNotNull)
    }

    private fun CurrenciesBalanceRecord.toDomain() = Balance(
        playerId.let(::MfPlayerId),
        currencyId.let(::CurrencyId),
        version,
        balance
    )
}