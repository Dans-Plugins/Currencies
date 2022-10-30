package com.dansplugins.currencies.listener

import com.dansplugins.currencies.Currencies
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareItemCraftEvent

class PrepareItemCraftListener(private val plugin: Currencies) : Listener {

    @EventHandler
    fun onPrepareItemCraft(event: PrepareItemCraftEvent) {
        if (!plugin.config.getBoolean("currencies.disallowCrafting")) return
        val player = event.viewers.firstOrNull() as? Player ?: return
        val inventory = event.inventory
        val currencyService = plugin.services.currencyService
        inventory.contents.filterNotNull().forEach { item ->
            if (currencyService.currencies.any { it.item.isSimilar(item) }) {
                player.sendMessage("${ChatColor.RED}You cannot use currencies when crafting.")
                event.inventory.result = null
                return
            }
        }
    }

}