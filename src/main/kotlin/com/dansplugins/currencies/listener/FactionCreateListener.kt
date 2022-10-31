package com.dansplugins.currencies.listener

import com.dansplugins.currencies.Currencies
import com.dansplugins.factionsystem.event.faction.FactionCreateEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class FactionCreateListener(private val plugin: Currencies) : Listener {

    @EventHandler
    fun onFactionCreate(event: FactionCreateEvent) {
        val medievalFactions = plugin.medievalFactions
        val medievalFactionsPermissions = medievalFactions.factionPermissions
        val currenciesPermissions = plugin.factionPermissions
        event.faction = event.faction.copy(
            defaultPermissionsByName = event.faction.defaultPermissionsByName + mapOf(
                medievalFactionsPermissions.setRolePermission(currenciesPermissions.createCurrency).name to false,
                currenciesPermissions.createCurrency.name to false
            ),
            roles = event.faction.roles.copy(roles = event.faction.roles.map { role ->
                val permissionsToAdd = buildMap {
                    if (role.hasPermission(event.faction, medievalFactionsPermissions.setRolePermission(
                            medievalFactionsPermissions.setRolePermission(
                                medievalFactionsPermissions.changeName
                            )
                    ))) {
                        put(medievalFactionsPermissions.setRolePermission(
                            medievalFactionsPermissions.setRolePermission(
                                currenciesPermissions.createCurrency
                            )
                        ).name, true)
                    }
                    if (role.hasPermission(event.faction, medievalFactionsPermissions.setRolePermission(
                            medievalFactionsPermissions.changeName))) {
                        put(medievalFactionsPermissions.setRolePermission(currenciesPermissions.createCurrency).name, true)
                    }
                    if (role.hasPermission(event.faction, medievalFactionsPermissions.changeName)) {
                        put(currenciesPermissions.createCurrency.name, true)
                    }
                }
                role.copy(permissionsByName = role.permissionsByName + permissionsToAdd)
            })
        )
    }

}