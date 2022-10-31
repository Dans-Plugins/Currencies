package com.dansplugins.currencies.permission.permissions

import com.dansplugins.currencies.Currencies
import com.dansplugins.currencies.currency.Currency
import com.dansplugins.currencies.currency.CurrencyId
import com.dansplugins.factionsystem.faction.MfFactionId
import com.dansplugins.factionsystem.faction.permission.MfFactionPermission
import com.dansplugins.factionsystem.faction.permission.MfFactionPermissionType
import com.dansplugins.factionsystem.faction.role.MfFactionRoleId

class RetireCurrency(private val plugin: Currencies) : MfFactionPermissionType() {
    override fun parse(name: String): MfFactionPermission? =
        if (name.matches(Regex("RETIRE_CURRENCY\\((.+)\\)"))) {
            Regex("RETIRE_CURRENCY\\((.+)\\)").find(name)
                ?.groupValues?.get(1)
                ?.let { plugin.services.currencyService.getCurrency(CurrencyId(it)) }
                ?.let { permissionFor(it) }
        } else {
            null
        }

    override fun permissionsFor(factionId: MfFactionId, roleIds: List<MfFactionRoleId>): List<MfFactionPermission> =
        plugin.services.currencyService.getCurrencies(factionId)
            .map { permissionFor(it) }


    private fun permissionFor(currency: Currency) = MfFactionPermission(
        "RETIRE_CURRENCY(${currency.id.value})",
        "Retire currency - ${currency.name}",
        false
    )

}