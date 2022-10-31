package com.dansplugins.currencies.listener

import com.dansplugins.currencies.Currencies
import com.dansplugins.currencies.balance.Balance
import com.dansplugins.currencies.coinpurse.CoinpurseInventoryHolder
import com.dansplugins.factionsystem.player.MfPlayerId
import dev.forkhandles.result4k.onFailure
import org.bukkit.ChatColor.RED
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCloseEvent
import java.util.logging.Level.SEVERE

class InventoryCloseListener(private val plugin: Currencies) : Listener {

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        val holder = event.inventory.holder
        if (holder is CoinpurseInventoryHolder) {
            val player = event.player
            if (player !is Player) return
            val items = event.inventory.contents
            val currencyService = plugin.services.currencyService
            val balanceService = plugin.services.balanceService
            plugin.server.scheduler.runTaskAsynchronously(plugin, Runnable {
                val itemsByCurrency = items.filterNotNull().groupBy { item ->
                    currencyService.currencies.singleOrNull { currency ->
                        currency.item.isSimilar(item)
                    }
                }
                val newBalances = itemsByCurrency.mapValues { (_, items) ->
                    items.sumOf { it.amount }
                }
                itemsByCurrency[null]?.forEach { item ->
                    plugin.server.scheduler.runTask(plugin, Runnable {
                        player.world.dropItem(player.location, item)
                    })
                }
                newBalances.forEach { (currency, newBalance) ->
                    if (currency != null) {
                        val balance = balanceService.getBalance(MfPlayerId.fromBukkitPlayer(player), currency.id)
                            ?.copy(balance = newBalance)
                            ?: Balance(MfPlayerId.fromBukkitPlayer(player), currency.id, balance = newBalance)
                        balanceService.save(balance).onFailure {
                            player.sendMessage("${RED}Failed to save balance for ${currency.name}: ${it.reason.message}")
                            plugin.logger.log(SEVERE, "Failed to save balance: ${it.reason.message}", it.reason.cause)
                            return@forEach
                        }
                    }
                }
            })
        }
    }

}