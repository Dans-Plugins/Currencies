package com.dansplugins.currencies.listener

import com.dansplugins.currencies.Currencies
import org.bukkit.ChatColor.RED
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareAnvilEvent

class PrepareAnvilListener(private val plugin: Currencies) : Listener {

    @EventHandler
    fun onPrepareAnvil(event: PrepareAnvilEvent) {
        if (!plugin.config.getBoolean("currencies.disallowAnvilUsage")) return
        val player = event.viewers.firstOrNull() as? Player ?: return
        val inventory = event.inventory
        val currencyService = plugin.services.currencyService
        inventory.contents.filterNotNull().forEach { item ->
            if (currencyService.currencies.any { it.item.isSimilar(item) }) {
                player.sendMessage("${RED}You cannot use currencies when renaming or repairing.")
                event.result = null
                return
            }
        }
    }

}