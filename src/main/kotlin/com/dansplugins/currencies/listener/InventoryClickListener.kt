package com.dansplugins.currencies.listener

import com.dansplugins.currencies.Currencies
import org.bukkit.ChatColor.RED
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType.SWAP_OFFHAND
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType.BLAST_FURNACE
import org.bukkit.event.inventory.InventoryType.FURNACE
import org.bukkit.event.inventory.InventoryType.SMOKER

internal val SMELTING_INVENTORY_TYPES = setOf(FURNACE, BLAST_FURNACE, SMOKER)

class InventoryClickListener(private val plugin: Currencies) : Listener {

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if (!plugin.config.getBoolean("currencies.disallowSmelting")) return
        if (event.inventory.type !in SMELTING_INVENTORY_TYPES) return
        val items = buildList {
            add(event.currentItem)
            // Items that would be put into the clicked slot only matter when that slot is in the furnace itself
            if (event.clickedInventory?.type in SMELTING_INVENTORY_TYPES) {
                add(event.cursor)
                if (event.hotbarButton >= 0) add(event.whoClicked.inventory.getItem(event.hotbarButton))
                if (event.click == SWAP_OFFHAND) add(event.whoClicked.inventory.itemInOffHand)
            }
        }
        val currencyService = plugin.services.currencyService
        if (items.none { item -> currencyService.currencies.any { it.item.isSimilar(item) } }) return
        event.whoClicked.sendMessage("${RED}You can't smelt a currency.")
        event.isCancelled = true
    }

}
