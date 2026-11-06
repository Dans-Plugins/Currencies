package com.dansplugins.currencies.currency

import com.dansplugins.factionsystem.faction.MfFactionId
import com.dansplugins.factionsystem.failure.OptimisticLockingFailureException
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.UnsafeValues
import org.bukkit.inventory.ItemFactory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.jooq.SQLDialect
import org.jooq.conf.Settings
import org.jooq.exception.DataAccessException
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
 * Characterizes the jOOQ currency repository against an embedded, in-memory H2 database built from
 * the plugin's own migration script, in the same MySQL compatibility mode and H2 dialect the
 * default `config.yml` uses. No network or external database is involved; each test gets a fresh
 * database.
 *
 * The item column goes through the real Bukkit object serialization. Only the two server lookups
 * that serialization makes are stubbed, and item meta is absent because it needs a real server.
 */
class JooqCurrencyRepositoryTest {

    private val goldId = CurrencyId("gold")
    private val silverId = CurrencyId("silver")
    private val factionA = MfFactionId("faction-a")
    private val factionB = MfFactionId("faction-b")

    private lateinit var connection: Connection
    private lateinit var repository: JooqCurrencyRepository

    @BeforeEach
    fun setUp() {
        stubItemSerialization()
        // The connection is held for the whole test: an in-memory H2 database lives exactly as long
        // as its last open connection, so closing it in tearDown discards the database too.
        connection = DriverManager.getConnection(
            "jdbc:h2:mem:currency-${UUID.randomUUID()};MODE=MYSQL;DATABASE_TO_UPPER=false"
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
        repository = JooqCurrencyRepository(dsl)
    }

    @AfterEach
    fun tearDown() {
        connection.close()
        unmockkStatic(Bukkit::class)
    }

    private fun stubItemSerialization() {
        val unsafe = mockk<UnsafeValues>()
        every { unsafe.dataVersion } returns 3120
        every { unsafe.getMaterial(any(), any()) } answers { Material.valueOf(firstArg()) }
        val itemFactory = mockk<ItemFactory>()
        every { itemFactory.getItemMeta(any()) } returns null
        every { itemFactory.equals(any<ItemMeta>(), any<ItemMeta>()) } answers { firstArg<Any?>() == secondArg<Any?>() }
        mockkStatic(Bukkit::class)
        every { Bukkit.getUnsafe() } returns unsafe
        every { Bukkit.getItemFactory() } returns itemFactory
    }

    private fun gold(version: Int = 0) = Currency(
        id = goldId,
        version = version,
        factionId = factionA,
        name = "Gold",
        item = ItemStack(Material.GOLD_INGOT)
    )

    @Test
    fun `a currency that was never stored is absent`() {
        assertNull(repository.getCurrency(goldId))
        assertTrue(repository.getCurrencies().isEmpty())
    }

    @Test
    fun `inserting a new currency stores it at version 1`() {
        val saved = repository.upsert(gold())

        assertEquals(gold(version = 1), saved)
        assertEquals(saved, repository.getCurrency(goldId))
    }

    @Test
    fun `an insert ignores the passed version and always starts at version 1`() {
        val saved = repository.upsert(gold(version = 7))

        assertEquals(1, saved.version)
    }

    @Test
    fun `every field of a currency is read back as it was stored`() {
        val currency = Currency(
            id = goldId,
            factionId = factionA,
            name = "Gold",
            description = "Struck in the capital",
            item = ItemStack(Material.EMERALD, 3),
            amount = 42,
            status = CurrencyStatus.RETIRED,
            legacyId = 17
        )

        assertEquals(currency.copy(version = 1), repository.upsert(currency))
    }

    @Test
    fun `a currency with no legacy ID reads back with none`() {
        repository.upsert(gold())

        assertNull(repository.getCurrency(goldId)?.legacyId)
    }

    @Test
    fun `updating with the current version replaces every field and bumps the version`() {
        val first = repository.upsert(gold())
        val changed = first.copy(
            factionId = factionB,
            name = "Royal Gold",
            description = "Reissued",
            item = ItemStack(Material.EMERALD),
            amount = 25,
            status = CurrencyStatus.RETIRED,
            legacyId = 3
        )

        val second = repository.upsert(changed)

        assertEquals(changed.copy(version = 2), second)
        assertEquals(second, repository.getCurrency(goldId))
    }

    @Test
    fun `updating with a stale version is rejected and leaves the stored currency untouched`() {
        val first = repository.upsert(gold())
        val second = repository.upsert(first.copy(amount = 25))

        // A second writer still holding version 1 must not overwrite the version-2 row.
        assertThrows<OptimisticLockingFailureException> {
            repository.upsert(first.copy(amount = 999))
        }

        assertEquals(second, repository.getCurrency(goldId))
    }

    @Test
    fun `an update with a version ahead of the stored one is rejected`() {
        val first = repository.upsert(gold())

        assertThrows<OptimisticLockingFailureException> {
            repository.upsert(first.copy(version = 5, amount = 999))
        }

        assertEquals(first, repository.getCurrency(goldId))
    }

    @Test
    fun `a name held by another currency is refused by the database and that currency is untouched`() {
        val first = repository.upsert(gold())

        assertThrows<DataAccessException> {
            repository.upsert(gold().copy(id = silverId))
        }

        assertNull(repository.getCurrency(silverId))
        assertEquals(first, repository.getCurrency(goldId))
    }

    @Test
    fun `currencies are listed across factions and looked up by their own ID`() {
        val gold = repository.upsert(gold())
        val silver = repository.upsert(gold().copy(id = silverId, factionId = factionB, name = "Silver"))

        assertEquals(setOf(gold, silver), repository.getCurrencies().toSet())
        assertEquals(2, repository.getCurrencies().size)
        assertEquals(gold, repository.getCurrency(goldId))
        assertEquals(silver, repository.getCurrency(silverId))
    }

    private companion object {
        const val MIGRATION = "/com/dansplugins/currencies/db/migration/V1__Initial_version.sql"
    }
}
