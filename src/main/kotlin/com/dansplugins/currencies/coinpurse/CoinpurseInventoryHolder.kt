package com.dansplugins.currencies.coinpurse

import com.dansplugins.currencies.Currencies
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

class CoinpurseInventoryHolder(plugin: Currencies) : InventoryHolder {

    private val _inventory: Inventory = plugin.server.createInventory(this, plugin.config.getInt("coinpurse.slots"), "Coinpurse")

    override fun getInventory(): Inventory {
        return _inventory
    }

}