package com.dansplugins.currencies.balance

import com.dansplugins.currencies.currency.CurrencyId
import com.dansplugins.factionsystem.failure.OptimisticLockingFailureException
import com.dansplugins.factionsystem.player.MfPlayerId
import org.jooq.SQLDialect
import org.jooq.conf.Settings
import org.jooq.impl.DSL
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.sql.Connection
import java.sql.DriverManager
import java.util.UUID

/**
 * Characterizes the jOOQ balance repository against an embedded, in-memory H2 database built from
 * the plugin's own migration script, in the same MySQL compatibility mode and H2 dialect the
 * default `config.yml` uses. No network or external database is involved; each test gets a fresh
 * database.
 */
class JooqBalanceRepositoryTest {

    private val alice = MfPlayerId("alice")
    private val bob = MfPlayerId("bob")
    private val gold = CurrencyId("gold")
    private val silver = CurrencyId("silver")

    private lateinit var connection: Connection
    private lateinit var repository: JooqBalanceRepository

    @BeforeEach
    fun setUp() {
        // The connection is held for the whole test: an in-memory H2 database lives exactly as long
        // as its last open connection, so closing it in tearDown discards the database too.
        connection = DriverManager.getConnection(
            "jdbc:h2:mem:balance-${UUID.randomUUID()};MODE=MYSQL;DATABASE_TO_UPPER=false"
        )
        // The plugin's own migration script, applied directly rather than through Flyway: Flyway
        // logs at info through Medieval Factions' SLF4J binding on the test classpath, which needs
        // a Bukkit server that does not exist here.
        val migration = requireNotNull(javaClass.getResource(MIGRATION)) { "missing $MIGRATION" }.readText()
        connection.createStatement().use { statement ->
            migration.split(";").map(String::trim).filter(String::isNotEmpty).forEach(statement::execute)
        }
        // As in Currencies.initialize: jOOQ's one-time logo and tip go through that same SLF4J
        // binding, from a static initializer whose failure would poison jOOQ for the whole JVM.
        System.setProperty("org.jooq.no-logo", "true")
        System.setProperty("org.jooq.no-tips", "true")
        val dsl = DSL.using(connection, SQLDialect.H2, Settings().withRenderSchema(false))
        repository = JooqBalanceRepository(dsl)
    }

    @AfterEach
    fun tearDown() {
        connection.close()
    }

    @Test
    fun `a balance that was never stored is absent`() {
        assertNull(repository.getBalance(alice, gold))
        assertTrue(repository.getBalances(alice).isEmpty())
    }

    @Test
    fun `inserting a new balance stores it at version 1`() {
        val saved = repository.upsert(Balance(alice, gold, balance = 10))

        assertEquals(Balance(alice, gold, version = 1, balance = 10), saved)
        assertEquals(saved, repository.getBalance(alice, gold))
    }

    @Test
    fun `an insert ignores the passed version and always starts at version 1`() {
        val saved = repository.upsert(Balance(alice, gold, version = 7, balance = 10))

        assertEquals(1, saved.version)
    }

    @Test
    fun `updating with the current version replaces the balance and bumps the version`() {
        val first = repository.upsert(Balance(alice, gold, balance = 10))

        val second = repository.upsert(first.copy(balance = 25))

        assertEquals(Balance(alice, gold, version = 2, balance = 25), second)
        assertEquals(second, repository.getBalance(alice, gold))
    }

    @Test
    fun `updating with a stale version is rejected and leaves the stored balance untouched`() {
        val first = repository.upsert(Balance(alice, gold, balance = 10))
        repository.upsert(first.copy(balance = 25))

        // A second writer still holding version 1 must not overwrite the version-2 row.
        assertThrows<OptimisticLockingFailureException> {
            repository.upsert(first.copy(balance = 999))
        }

        assertEquals(Balance(alice, gold, version = 2, balance = 25), repository.getBalance(alice, gold))
    }

    @Test
    fun `an update with a version ahead of the stored one is rejected`() {
        repository.upsert(Balance(alice, gold, balance = 10))

        assertThrows<OptimisticLockingFailureException> {
            repository.upsert(Balance(alice, gold, version = 5, balance = 999))
        }

        assertEquals(10, repository.getBalance(alice, gold)?.balance)
    }

    @Test
    fun `balances are keyed by player and currency together`() {
        repository.upsert(Balance(alice, gold, balance = 10))
        repository.upsert(Balance(alice, silver, balance = 4))
        repository.upsert(Balance(bob, gold, balance = 3))

        assertEquals(10, repository.getBalance(alice, gold)?.balance)
        assertEquals(4, repository.getBalance(alice, silver)?.balance)
        assertEquals(3, repository.getBalance(bob, gold)?.balance)
        assertNull(repository.getBalance(bob, silver))
    }

    @Test
    fun `a player's balances are listed across currencies without other players' balances`() {
        repository.upsert(Balance(alice, gold, balance = 10))
        repository.upsert(Balance(alice, silver, balance = 4))
        repository.upsert(Balance(bob, gold, balance = 3))

        val balances = repository.getBalances(alice)

        assertEquals(
            setOf(Balance(alice, gold, version = 1, balance = 10), Balance(alice, silver, version = 1, balance = 4)),
            balances.toSet()
        )
        assertEquals(2, balances.size)
    }

    @Test
    fun `a zero balance is stored and read back rather than treated as absent`() {
        repository.upsert(Balance(alice, gold, balance = 0))

        assertEquals(0, repository.getBalance(alice, gold)?.balance)
    }

    private companion object {
        const val MIGRATION = "/com/dansplugins/currencies/db/migration/V1__Initial_version.sql"
    }
}
