package com.dansplugins.currencies

import com.dansplugins.currencies.balance.BalanceService
import com.dansplugins.currencies.balance.JooqBalanceRepository
import com.dansplugins.currencies.command.coinpurse.CoinpurseCommand
import com.dansplugins.currencies.command.currency.CurrencyCommand
import com.dansplugins.currencies.currency.CurrencyService
import com.dansplugins.currencies.currency.JooqCurrencyRepository
import com.dansplugins.currencies.legacy.CurrenciesLegacyDataMigrator
import com.dansplugins.currencies.listener.*
import com.dansplugins.currencies.permission.CurrenciesFactionPermissions
import com.dansplugins.currencies.service.Services
import com.dansplugins.factionsystem.MedievalFactions
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.forkhandles.result4k.onFailure
import org.bstats.bukkit.Metrics
import org.bukkit.plugin.java.JavaPlugin
import org.flywaydb.core.Flyway
import org.jooq.SQLDialect
import org.jooq.conf.Settings
import org.jooq.impl.DSL
import preponderous.ponder.minecraft.bukkit.plugin.registerListeners
import java.util.logging.Level.SEVERE
import javax.sql.DataSource

private const val MEDIEVAL_FACTIONS_PLUGIN_NAME = "MedievalFactions"

class Currencies : JavaPlugin() {

    lateinit var medievalFactions: MedievalFactions

    private lateinit var dataSource: DataSource

    lateinit var factionPermissions: CurrenciesFactionPermissions
    lateinit var services: Services

    override fun onEnable() {
        val migrator = CurrenciesLegacyDataMigrator(this)
        if (config.getString("version")?.startsWith("v1.") == true) {
            migrator.backup()
            saveDefaultConfig()
            reloadConfig()
            config.options().copyDefaults(true)
            config.set("migrateCurrencies1", true)
            saveConfig()
            logger.warning("Shutting down the server due to Currencies 1 migration.")
            logger.warning("If you have a database, please configure it before starting the server again.")
            logger.warning("Otherwise, simply start your server again to begin migration.")
            server.shutdown()
            return
        }

        saveDefaultConfig()
        config.options().copyDefaults(true)
        config.set("version", description.version)
        saveConfig()

        Metrics(this, 12810)

        if (!initializeMedievalFactions()) {
            isEnabled = false
            return
        }

        Class.forName("org.h2.Driver")
        val hikariConfig = HikariConfig()
        hikariConfig.jdbcUrl = config.getString("database.url")
        val databaseUsername = config.getString("database.username")
        if (databaseUsername != null) {
            hikariConfig.username = databaseUsername
        }
        val databasePassword = config.getString("database.password")
        if (databasePassword != null) {
            hikariConfig.password = databasePassword
        }
        dataSource = HikariDataSource(hikariConfig)
        val oldClassLoader = Thread.currentThread().contextClassLoader
        Thread.currentThread().contextClassLoader = classLoader
        val flyway = Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:com/dansplugins/currencies/db/migration")
            .table("currencies_schema_history")
            .baselineOnMigrate(true)
            .baselineVersion("0")
            .validateOnMigrate(false)
            .load()
        flyway.migrate()
        Thread.currentThread().contextClassLoader = oldClassLoader

        System.setProperty("org.jooq.no-logo", "true")
        System.setProperty("org.jooq.no-tips", "true")

        val dialect = config.getString("database.dialect")?.let(SQLDialect::valueOf)
        val jooqSettings = Settings().withRenderSchema(false)
        val dsl = DSL.using(
            dataSource,
            dialect,
            jooqSettings
        )

        factionPermissions = CurrenciesFactionPermissions(this)

        val currencyRepository = JooqCurrencyRepository(dsl)
        val balanceRepository = JooqBalanceRepository(dsl)

        val currencyService = CurrencyService(this, currencyRepository)
        val balanceService = BalanceService(this, balanceRepository)

        services = Services(
            currencyService,
            balanceService
        )

        val factionService = medievalFactions.services.factionService
        logger.info("Adding Currencies faction permissions to existing factions...")
        var factionsUpdated = 0
        factionService.factions.forEach { faction ->
            if (!faction.defaultPermissions.containsKey(factionPermissions.createCurrency)) {
                factionService.save(
                    faction.copy(
                        defaultPermissionsByName = faction.defaultPermissionsByName + mapOf(
                            medievalFactions.factionPermissions.setRolePermission(factionPermissions.createCurrency).name to false,
                            factionPermissions.createCurrency.name to false
                        ),
                        roles = faction.roles.copy(roles = faction.roles.map { role ->
                            val permissionsToAdd = buildMap {
                                if (role.hasPermission(faction, medievalFactions.factionPermissions.setRolePermission(
                                        medievalFactions.factionPermissions.setRolePermission(
                                            medievalFactions.factionPermissions.changeName
                                        )
                                    ))) {
                                    put(medievalFactions.factionPermissions.setRolePermission(
                                        medievalFactions.factionPermissions.setRolePermission(
                                            factionPermissions.createCurrency
                                        )
                                    ).name, true)
                                }
                                if (role.hasPermission(faction, medievalFactions.factionPermissions.setRolePermission(medievalFactions.factionPermissions.changeName))) {
                                    put(medievalFactions.factionPermissions.setRolePermission(factionPermissions.createCurrency).name, true)
                                }
                                if (role.hasPermission(faction, medievalFactions.factionPermissions.changeName)) {
                                    put(factionPermissions.createCurrency.name, true)
                                }
                            }
                            role.copy(permissionsByName = role.permissionsByName + permissionsToAdd)
                        })
                    )
                ).onFailure {
                    logger.log(SEVERE, "Failed to save faction: ${it.reason.message}", it.reason.cause)
                    return@forEach
                }
                factionsUpdated++
            }
        }
        logger.info("Added Currencies faction permissions to $factionsUpdated factions.")

        if (config.getBoolean("migrateCurrencies1")) {
            migrator.migrate()
            config.set("migrateCurrencies1", null)
            saveConfig()
        }

        registerListeners(
            BlockPlaceListener(this),
            FactionCreateListener(this),
            FactionDisbandListener(this),
            InventoryClickListener(this),
            InventoryCloseListener(this),
            PrepareAnvilListener(this),
            PrepareItemCraftListener(this)
        )

        getCommand("coinpurse")?.setExecutor(CoinpurseCommand(this))
        getCommand("currency")?.setExecutor(CurrencyCommand(this))
    }

    private fun initializeMedievalFactions(): Boolean {
        logger.info("Starting initialization of the Medieval Factions integration...")

        val pluginManager = server.pluginManager

        val medievalFactionsFound = pluginManager.plugins.any { it.name == MEDIEVAL_FACTIONS_PLUGIN_NAME }
        if (!medievalFactionsFound) {
            logger.severe("Medieval Factions was not found in the list of plugins provided by the plugin manager.")
            logger.info("Available plugins: ${pluginManager.plugins.joinToString { it.name }}")
            return false
        }

        val medievalFactionsPlugin = pluginManager.getPlugin(MEDIEVAL_FACTIONS_PLUGIN_NAME)
        if (medievalFactionsPlugin == null) {
            logger.severe("Medieval Factions could not be retrieved from the plugin manager.")
            return false
        }

        logger.info("""
            Medieval Factions Details:
            - Version: ${medievalFactionsPlugin.description.version}
            - Actual Class: ${medievalFactionsPlugin.javaClass.name}
            - Expected Class: ${MedievalFactions::class.java.name}
            - Plugin ClassLoader: ${medievalFactionsPlugin.javaClass.classLoader}
            - Expected ClassLoader: ${MedievalFactions::class.java.classLoader}
        """.trimIndent())

        val medievalFactions = try {
            // First try to verify if it's the correct instance
            if (!MedievalFactions::class.java.isInstance(medievalFactionsPlugin)) {
                logger.severe("Medieval Factions plugin is not an instance of the expected MedievalFactions class")
                return false
            }
            medievalFactionsPlugin as? MedievalFactions
        } catch (e: Exception) {
            logger.severe("An exception occurred while attempting to cast Medieval Factions to the MedievalFactions class:")
            logger.severe("Error message: ${e.message}")
            e.printStackTrace()
            return false
        }

        if (medievalFactions == null) {
            logger.severe("Failed to cast the Medieval Factions plugin to the expected MedievalFactions class. Ensure the plugin version is compatible.")
            return false
        }

        this.medievalFactions = medievalFactions
        logger.info("Medieval Factions successfully initialized.")
        return true
    }

}