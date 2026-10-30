package com.dansplugins.currencies.permission

import com.dansplugins.currencies.Currencies
import com.dansplugins.currencies.currency.CurrencyId
import com.dansplugins.currencies.permission.permissions.ChangeCurrencyDescription
import com.dansplugins.currencies.permission.permissions.ChangeCurrencyName
import com.dansplugins.currencies.permission.permissions.MintCurrency
import com.dansplugins.currencies.permission.permissions.RetireCurrency
import com.dansplugins.factionsystem.faction.permission.MfFactionPermission
import com.dansplugins.factionsystem.faction.permission.MfFactionPermissionType
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class CurrenciesFactionPermissionsTest {

    private val plugin = mockk<Currencies>(relaxed = true)
    private val medievalFactionsPermissions = plugin.medievalFactions.factionPermissions

    private val createCurrencyType = mockk<MfFactionPermissionType>()
    private val registered = mutableListOf<MfFactionPermissionType>()

    init {
        every { medievalFactionsPermissions.wrapSimplePermission("CREATE_CURRENCY", "Create currency", false) } returns createCurrencyType
        every { medievalFactionsPermissions.addPermissionType(any()) } answers { registered += firstArg<MfFactionPermissionType>() }
        every { medievalFactionsPermissions.parse(any()) } answers { MfFactionPermission(firstArg(), "Parsed", false) }
    }

    private val permissions = CurrenciesFactionPermissions(plugin)

    @Test
    fun `registers the create permission and every per-currency permission type with Medieval Factions`() {
        assertEquals(5, registered.size)
        assertSame(createCurrencyType, registered[0])
        assertEquals(
            listOf(RetireCurrency::class, ChangeCurrencyName::class, ChangeCurrencyDescription::class, MintCurrency::class),
            registered.drop(1).map { it::class }
        )
    }

    @Test
    fun `create currency is parsed from its simple name`() {
        assertEquals("CREATE_CURRENCY", permissions.createCurrency.name)
    }

    @Test
    fun `per-currency permissions are parsed from names embedding the currency id`() {
        val id = CurrencyId("gold")
        permissions.retireCurrency(id)
        permissions.changeCurrencyName(id)
        permissions.changeCurrencyDescription(id)
        permissions.mintCurrency(id)

        verify {
            medievalFactionsPermissions.parse("RETIRE_CURRENCY(gold)")
            medievalFactionsPermissions.parse("CHANGE_CURRENCY_NAME(gold)")
            medievalFactionsPermissions.parse("CHANGE_CURRENCY_DESCRIPTION(gold)")
            medievalFactionsPermissions.parse("MINT_CURRENCY(gold)")
        }
        assertEquals("MINT_CURRENCY(gold)", permissions.mintCurrency(id).name)
    }

}
