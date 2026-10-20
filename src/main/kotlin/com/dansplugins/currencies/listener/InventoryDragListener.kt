package com.dansplugins.currencies.listener

import com.dansplugins.currencies.Currencies
import org.bukkit.ChatColor.RED
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryDragEvent

class InventoryDragListener(private val plugin: Currencies) : Listener {

    @EventHandler
    fun onInventoryDrag(event: InventoryDragEvent) {
        if (!plugin.config.getBoolean("currencies.disallowSmelting")) return
        if (event.inventory.type !in SMELTING_INVENTORY_TYPES) return
        // Raw slots below the top inventory's size belong to the furnace; the rest are the player's inventory
        if (event.rawSlots.none { it < event.inventory.size }) return
        val currencyService = plugin.services.currencyService
        if (currencyService.currencies.none { it.item.isSimilar(event.oldCursor) }) return
        event.whoClicked.sendMessage("${RED}You can't smelt a currency.")
        event.isCancelled = true
    }

}
