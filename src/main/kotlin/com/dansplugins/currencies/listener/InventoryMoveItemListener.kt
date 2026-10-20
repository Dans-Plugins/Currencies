package com.dansplugins.currencies.listener

import com.dansplugins.currencies.Currencies
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryMoveItemEvent

class InventoryMoveItemListener(private val plugin: Currencies) : Listener {

    @EventHandler
    fun onInventoryMoveItem(event: InventoryMoveItemEvent) {
        if (!plugin.config.getBoolean("currencies.disallowSmelting")) return
        if (event.destination.type !in SMELTING_INVENTORY_TYPES) return
        val currencyService = plugin.services.currencyService
        if (currencyService.currencies.none { it.item.isSimilar(event.item) }) return
        event.isCancelled = true
    }

}
