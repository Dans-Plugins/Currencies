package com.dansplugins.currencies.permission

import com.dansplugins.currencies.Currencies
import com.dansplugins.currencies.currency.CurrencyId
import com.dansplugins.currencies.permission.permissions.ChangeCurrencyDescription
import com.dansplugins.currencies.permission.permissions.ChangeCurrencyName
import com.dansplugins.currencies.permission.permissions.MintCurrency
import com.dansplugins.currencies.permission.permissions.RetireCurrency

class CurrenciesFactionPermissions(private val plugin: Currencies) {

    private val medievalFactions = plugin.medievalFactions

    init {
        val permissions = medievalFactions.factionPermissions
        permissions.addPermissionType(permissions.wrapSimplePermission("CREATE_CURRENCY", "Create currency", false))
        permissions.addPermissionType(RetireCurrency(plugin))
        permissions.addPermissionType(ChangeCurrencyName(plugin))
        permissions.addPermissionType(ChangeCurrencyDescription(plugin))
        permissions.addPermissionType(MintCurrency(plugin))
    }

    val createCurrency = medievalFactions.factionPermissions.parse("CREATE_CURRENCY")!!
    fun retireCurrency(currencyId: CurrencyId) = medievalFactions.factionPermissions.parse("RETIRE_CURRENCY(${currencyId.value})")!!
    fun changeCurrencyName(currencyId: CurrencyId) = medievalFactions.factionPermissions.parse("CHANGE_CURRENCY_NAME(${currencyId.value})")!!
    fun changeCurrencyDescription(currencyId: CurrencyId) = medievalFactions.factionPermissions.parse("CHANGE_CURRENCY_DESCRIPTION(${currencyId.value})")!!
    fun mintCurrency(currencyId: CurrencyId) = medievalFactions.factionPermissions.parse("MINT_CURRENCY(${currencyId.value})")!!

}