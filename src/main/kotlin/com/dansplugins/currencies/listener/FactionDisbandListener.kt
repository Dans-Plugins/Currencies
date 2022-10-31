package com.dansplugins.currencies.listener

import com.dansplugins.currencies.Currencies
import com.dansplugins.currencies.currency.CurrencyStatus.RETIRED
import com.dansplugins.factionsystem.event.faction.FactionDisbandEvent
import dev.forkhandles.result4k.onFailure
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.util.logging.Level.SEVERE

class FactionDisbandListener(private val plugin: Currencies) : Listener {

    @EventHandler
    fun onFactionDisband(event: FactionDisbandEvent) {
        val currencyService = plugin.services.currencyService
        currencyService.getCurrencies(event.factionId).forEach { currency ->
            if (currency.status != RETIRED) {
                currencyService.save(currency.copy(status = RETIRED)).onFailure {
                    plugin.logger.log(SEVERE, "Failed to save currency: ${it.reason.message}", it.reason.cause)
                    return@forEach
                }
            }
        }
    }

}