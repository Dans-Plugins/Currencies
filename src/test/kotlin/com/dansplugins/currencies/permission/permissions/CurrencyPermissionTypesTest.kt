package com.dansplugins.currencies.permission.permissions

import com.dansplugins.currencies.Currencies
import com.dansplugins.currencies.currency.Currency
import com.dansplugins.currencies.currency.CurrencyId
import com.dansplugins.currencies.currency.CurrencyRepository
import com.dansplugins.currencies.currency.CurrencyService
import com.dansplugins.factionsystem.faction.MfFaction
import com.dansplugins.factionsystem.faction.MfFactionId
import com.dansplugins.factionsystem.faction.permission.MfFactionPermissionType
import io.mockk.every
import io.mockk.mockk
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

/**
 * The four per-currency faction permission types share one shape: a `PREFIX(<currency id>)` name
 * that parses only for a known currency, and one permission per currency the faction owns.
 * Each case below runs against every type, so a type drifting from its siblings fails here.
 */
@Suppress("UNUSED_PARAMETER")
class CurrencyPermissionTypesTest {

    private val plugin = mockk<Currencies>(relaxed = true)
    private val faction = mockk<MfFaction>()

    private val gold = Currency(id = CurrencyId("gold"), factionId = MfFactionId("faction"), name = "Gold", item = mockk<ItemStack>())
    private val silver = Currency(id = CurrencyId("silver"), factionId = MfFactionId("faction"), name = "Silver", item = mockk<ItemStack>())
    private val copper = Currency(id = CurrencyId("copper"), factionId = MfFactionId("other-faction"), name = "Copper", item = mockk<ItemStack>())

    private class FakeCurrencyRepository(vararg initial: Currency) : CurrencyRepository {
        private val stored = initial.associateBy { it.id }
        override fun getCurrencies() = stored.values.toList()
        override fun getCurrency(id: CurrencyId) = stored[id]
        override fun upsert(currency: Currency) = currency
    }

    init {
        val currencyService = CurrencyService(plugin, FakeCurrencyRepository(gold, silver, copper))
        every { plugin.services.currencyService } returns currencyService
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("types")
    fun `parses its own name for a known currency`(prefix: String, label: String, create: (Currencies) -> MfFactionPermissionType) {
        val permission = create(plugin).parse("$prefix(gold)")

        assertNotNull(permission)
        assertEquals("$prefix(gold)", permission!!.name)
        assertEquals("$label - Gold", permission.translate(faction))
        assertFalse(permission.default)
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("types")
    fun `does not parse a currency id that does not exist`(prefix: String, label: String, create: (Currencies) -> MfFactionPermissionType) {
        assertNull(create(plugin).parse("$prefix(platinum)"))
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("types")
    fun `does not parse a name without a currency id`(prefix: String, label: String, create: (Currencies) -> MfFactionPermissionType) {
        val type = create(plugin)
        assertNull(type.parse(prefix))
        assertNull(type.parse("$prefix()"))
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("types")
    fun `does not parse another type's name`(prefix: String, label: String, create: (Currencies) -> MfFactionPermissionType) {
        val type = create(plugin)
        types().map { it.get()[0] as String }
            .filter { it != prefix }
            .forEach { otherPrefix -> assertNull(type.parse("$otherPrefix(gold)"), otherPrefix) }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("types")
    fun `does not parse a name with text around it`(prefix: String, label: String, create: (Currencies) -> MfFactionPermissionType) {
        val type = create(plugin)
        assertNull(type.parse("X$prefix(gold)"))
        assertNull(type.parse("$prefix(gold)X"))
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("types")
    fun `lists one permission per currency the faction owns`(prefix: String, label: String, create: (Currencies) -> MfFactionPermissionType) {
        val permissions = create(plugin).permissionsFor(MfFactionId("faction"), emptyList())

        assertEquals(
            setOf("$prefix(gold)" to "$label - Gold", "$prefix(silver)" to "$label - Silver"),
            permissions.map { it.name to it.translate(faction) }.toSet()
        )
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("types")
    fun `lists no permissions for a faction without currencies`(prefix: String, label: String, create: (Currencies) -> MfFactionPermissionType) {
        assertEquals(emptyList<Any>(), create(plugin).permissionsFor(MfFactionId("no-currencies"), emptyList()))
    }

    companion object {
        @JvmStatic
        fun types(): List<Arguments> = listOf(
            Arguments.of("MINT_CURRENCY", "Mint currency", { plugin: Currencies -> MintCurrency(plugin) }),
            Arguments.of("RETIRE_CURRENCY", "Retire currency", { plugin: Currencies -> RetireCurrency(plugin) }),
            Arguments.of("CHANGE_CURRENCY_NAME", "Change currency name", { plugin: Currencies -> ChangeCurrencyName(plugin) }),
            Arguments.of("CHANGE_CURRENCY_DESCRIPTION", "Change currency description", { plugin: Currencies -> ChangeCurrencyDescription(plugin) })
        )
    }

}
