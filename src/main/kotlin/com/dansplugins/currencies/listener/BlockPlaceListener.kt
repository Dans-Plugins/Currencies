package com.dansplugins.currencies.listener

import com.dansplugins.currencies.Currencies
import org.bukkit.ChatColor.RED
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent

class BlockPlaceListener(private val plugin: Currencies) : Listener {

    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        if (!plugin.config.getBoolean("currencies.disallowPlacement")) return
        val currencyService = plugin.services.currencyService
        if (currencyService.currencies.none { it.item.isSimilar(event.itemInHand) }) return
        event.player.sendMessage("${RED}You cannot place currencies.")
        event.isCancelled = true
    }

}