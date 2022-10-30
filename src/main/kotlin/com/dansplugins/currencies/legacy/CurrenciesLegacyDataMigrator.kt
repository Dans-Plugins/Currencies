package com.dansplugins.currencies.legacy

import com.dansplugins.currencies.Currencies
import com.dansplugins.currencies.balance.Balance
import com.dansplugins.currencies.currency.Currency
import com.dansplugins.currencies.currency.CurrencyStatus.ACTIVE
import com.dansplugins.currencies.currency.CurrencyStatus.RETIRED
import com.dansplugins.factionsystem.faction.MfFactionId
import com.dansplugins.factionsystem.player.MfPlayerId
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dev.forkhandles.result4k.onFailure
import org.bukkit.ChatColor.WHITE
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
import java.io.File
import java.time.Instant
import java.time.format.DateTimeFormatter.ISO_INSTANT
import java.util.logging.Level.SEVERE

class CurrenciesLegacyDataMigrator(private val plugin: Currencies) {

    fun backup() {
        plugin.logger.info("Backing up Currencies 1 data...")
        val startTime = System.currentTimeMillis()
        val backupFolder = File(plugin.dataFolder, "currencies1_backup")
        backupOldFiles(backupFolder)
        plugin.logger.info("Backed up Currencies 1 data (${System.currentTimeMillis() - startTime}ms)")
    }

    fun migrate() {
        plugin.logger.info("Migrating Currencies 1 data...")
        val startTime = System.currentTimeMillis()
        val backupFolder = File(plugin.dataFolder, "currencies1_backup")
        migrateConfig(File(backupFolder, "config.yml"))
        val gson = Gson()
        migrateActiveCurrencies(File(backupFolder, "activeCurrencies.json"), gson)
        migrateRetiredCurrencies(File(backupFolder, "retiredCurrencies.json"), gson)
        migrateCoinpurses(File(backupFolder, "coinpurses.json"), gson)
        plugin.logger.info("Migrated Currencies 1 data (${System.currentTimeMillis() - startTime}ms)")
    }

    private fun backupOldFiles(backupFolder: File) {
        val files = plugin.dataFolder.listFiles() ?: return
        plugin.logger.info("Backing up old files to \"${backupFolder.path}\"...")
        val startTime = System.currentTimeMillis()
        if (backupFolder.exists()) {
            backupFolder.renameTo(File("currencies1_backup_${ISO_INSTANT.format(Instant.ofEpochMilli(backupFolder.lastModified()))}"))
        }
        backupFolder.mkdirs()
        files.forEach { file ->
            file.renameTo(File(backupFolder, plugin.dataFolder.toURI().relativize(file.toURI()).path))
        }
        plugin.logger.info("Backup complete (${System.currentTimeMillis() - startTime}ms)")
    }

    private fun migrateConfig(oldConfigFile: File) {
        plugin.logger.info("Migrating config settings...")
        val startTime = System.currentTimeMillis()
        val oldConfig = YamlConfiguration.loadConfiguration(oldConfigFile)
        plugin.config.set("currencies.powerCostEnabled", oldConfig.getBoolean("powerCostEnabled"))
        plugin.config.set("currencies.powerCost", oldConfig.getDouble("powerCost"))
        plugin.config.set("currencies.disallowCrafting", oldConfig.getBoolean("disallowCrafting"))
        plugin.config.set("currencies.disallowSmelting", oldConfig.getBoolean("disallowSmelting"))
        plugin.config.set("currencies.disallowPlacement", oldConfig.getBoolean("disallowPlacement"))
        plugin.config.set("currencies.showAmountMinted", oldConfig.getBoolean("showAmountMinted"))
        plugin.config.set("currencies.disallowAnvilUsage", oldConfig.getBoolean("disallowAnvilUsage"))
        plugin.config.set("currencies.itemCostEnabled", oldConfig.getBoolean("itemCost"))
        plugin.saveConfig()
        plugin.logger.info("Config migration complete (${System.currentTimeMillis() - startTime}ms)")
    }

    private fun migrateActiveCurrencies(oldActiveCurrenciesFile: File, gson: Gson) {
        plugin.logger.info("Migrating active currencies...")
        val startTime = System.currentTimeMillis()
        val legacyCurrencies = gson.fromJson<List<LegacyCurrency>>(
            oldActiveCurrenciesFile.readText(),
            TypeToken.getParameterized(List::class.java, LegacyCurrency::class.java).type
        )
       migrateCurrencies(legacyCurrencies, gson)
        plugin.logger.info("Active currencies migrated (${System.currentTimeMillis() - startTime}ms)")
    }

    private fun migrateRetiredCurrencies(oldRetiredCurrenciesFile: File, gson: Gson) {
        plugin.logger.info("Migrating retired currencies...")
        val startTime = System.currentTimeMillis()
        val legacyCurrencies = gson.fromJson<List<LegacyCurrency>>(
            oldRetiredCurrenciesFile.readText(),
            TypeToken.getParameterized(List::class.java, LegacyCurrency::class.java).type
        )
        migrateCurrencies(legacyCurrencies, gson)
        plugin.logger.info("Retired currencies migrated (${System.currentTimeMillis() - startTime}ms)")
    }

    private fun migrateCurrencies(legacyCurrencies: List<LegacyCurrency>, gson: Gson) {
        val currencyService = plugin.services.currencyService
        val factionService = plugin.medievalFactions.services.factionService
        legacyCurrencies.forEach { legacyCurrency ->
            val name = gson.fromJson(legacyCurrency.name, String::class.java)
            val factionName = gson.fromJson(legacyCurrency.factionName, String::class.java)
            val faction = factionService.getFaction(factionName)
            val material = Material.valueOf(gson.fromJson(legacyCurrency.material, String::class.java))
            val legacyCurrencyId = gson.fromJson(legacyCurrency.currencyID, Int::class.javaObjectType)
            val amount = gson.fromJson(legacyCurrency.amount, Int::class.javaObjectType)
            val description = gson.fromJson(legacyCurrency.description, String::class.java)
            val isRetired = gson.fromJson(legacyCurrency.retired, Boolean::class.javaObjectType)

            val currency = currencyService.save(Currency(
                factionId = faction?.id ?: MfFactionId.generate(),
                name = name,
                description = description,
                item = ItemStack(material).apply {
                    itemMeta = (if (hasItemMeta()) itemMeta else plugin.server.itemFactory.getItemMeta(material))?.apply {
                        setDisplayName(name)
                        lore = listOf(
                            "",
                            "${WHITE}Currency of $factionName",
                            "${WHITE}currencyID: $legacyCurrencyId"
                        )
                    }
                },
                amount = amount,
                status = if (isRetired || faction == null) RETIRED else ACTIVE,
                legacyId = legacyCurrencyId
            )).onFailure {
                plugin.logger.log(SEVERE, "Failed to save currency \"${legacyCurrency.name}\": ${it.reason.message}", it.reason.cause)
                return@forEach
            }
            if (faction != null) {
                factionService.save(
                    faction.copy(
                        defaultPermissionsByName = faction.defaultPermissionsByName + mapOf(
                            plugin.factionPermissions.mintCurrency(currency.id).name to false,
                            plugin.factionPermissions.changeCurrencyName(currency.id).name to false,
                            plugin.factionPermissions.changeCurrencyDescription(currency.id).name to false
                        ),
                        roles = faction.roles.copy(roles = faction.roles.map { role ->
                            if (role.hasPermission(faction, plugin.factionPermissions.createCurrency)) {
                                role.copy(
                                    permissionsByName = role.permissionsByName + mapOf(
                                        plugin.factionPermissions.mintCurrency(currency.id).name to true,
                                        plugin.factionPermissions.changeCurrencyName(currency.id).name to true,
                                        plugin.factionPermissions.changeCurrencyDescription(currency.id).name to true
                                    )
                                )
                            } else {
                                role
                            }
                        })
                    )
                ).onFailure {
                    plugin.logger.log(
                        SEVERE,
                        "Failed to save faction \"${faction.name}\": ${it.reason.message}",
                        it.reason.cause
                    )
                    return@forEach
                }
            }
        }
    }

    private fun migrateCoinpurses(oldCoinpurseFile: File, gson: Gson) {
        plugin.logger.info("Migrating coinpurses...")
        val startTime = System.currentTimeMillis()
        val legacyCoinpurses = gson.fromJson<List<LegacyCoinpurse>>(
            oldCoinpurseFile.readText(),
            TypeToken.getParameterized(List::class.java, LegacyCoinpurse::class.java).type
        )
        val balanceService = plugin.services.balanceService
        val currencyService = plugin.services.currencyService
        legacyCoinpurses.forEach { legacyCoinpurse ->
            val ownerId = MfPlayerId(gson.fromJson(legacyCoinpurse.ownerUUID, String::class.java))
            gson.fromJson<Map<Int, Int>>(
                legacyCoinpurse.currencyAmounts,
                TypeToken.getParameterized(Map::class.java, Integer::class.javaObjectType, Integer::class.javaObjectType).type
            ).mapNotNull { (legacyCurrencyId, balance) ->
                currencyService.currencies.singleOrNull { it.legacyId == legacyCurrencyId }?.let { it to balance }
            }.forEach storeLegacyBalance@{ (currency, legacyBalance) ->
                val balance = balanceService.getBalance(ownerId, currency.id)?.copy(balance = legacyBalance)
                    ?: Balance(ownerId, currency.id, balance = legacyBalance)
                balanceService.save(balance).onFailure {
                    plugin.logger.log(SEVERE, "Failed to save balance for player \"${ownerId.value}\" in currency \"${currency.name}\": ${it.reason.message}", it.reason.cause)
                    return@storeLegacyBalance
                }
            }
        }
        plugin.logger.info("Coinpurses migrated (${System.currentTimeMillis() - startTime}ms)")
    }

}