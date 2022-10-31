package com.dansplugins.currencies.currency

import com.dansplugins.factionsystem.faction.MfFactionId
import org.bukkit.inventory.ItemStack

data class Currency(
    @get:JvmName("getId")
    val id: CurrencyId = CurrencyId.generate(),
    val version: Int = 0,
    @get:JvmName("getFactionId")
    val factionId: MfFactionId,
    val name: String,
    val description: String = "",
    val item: ItemStack,
    val amount: Int = 0,
    val status: CurrencyStatus = CurrencyStatus.ACTIVE,
    val legacyId: Int? = null
)