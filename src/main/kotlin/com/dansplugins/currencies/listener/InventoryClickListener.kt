package com.dansplugins.currencies.listener

import com.dansplugins.currencies.Currencies
import org.bukkit.ChatColor.RED
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType.BLAST_FURNACE
import org.bukkit.event.inventory.InventoryType.FURNACE

class InventoryClickListener(private val plugin: Currencies) : Listener {

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if (!plugin.config.getBoolean("currencies.disallowSmelting")) return
        if (event.inventory.type != FURNACE && event.inventory.type != BLAST_FURNACE) return
        val currencyService = plugin.services.currencyService
        if (currencyService.currencies.none { it.item.isSimilar(event.currentItem) }) return
        event.whoClicked.sendMessage("${RED}You can't smelt a currency.")
        event.isCancelled = true
    }

}