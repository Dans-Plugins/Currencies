package com.dansplugins.currencies

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.sql.DriverManager

class CurrenciesJdbcUrlTest {

    private val defaultUrl = "jdbc:h2:./medieval_factions_db;AUTO_SERVER=true;MODE=MYSQL;DATABASE_TO_UPPER=false"

    @Test
    fun hardenJdbcUrl_appendsCloseOnExitFalse_toTheDefaultH2Url() {
        assertEquals("$defaultUrl;DB_CLOSE_ON_EXIT=FALSE", Currencies.hardenJdbcUrl(defaultUrl))
    }

    @Test
    fun hardenJdbcUrl_leavesAnOperatorSettingAndOtherDatabasesAlone() {
        val explicit = "$defaultUrl;db_close_on_exit=TRUE"
        assertEquals(explicit, Currencies.hardenJdbcUrl(explicit))
        val mariadb = "jdbc:mariadb://localhost:3306/medievalfactions"
        assertEquals(mariadb, Currencies.hardenJdbcUrl(mariadb))
        assertEquals("", Currencies.hardenJdbcUrl(""))
    }

    @Test
    fun hardenJdbcUrl_producesAUrlTheBundledH2Accepts() {
        val url = Currencies.hardenJdbcUrl("jdbc:h2:mem:currencies;MODE=MYSQL;DATABASE_TO_UPPER=false")
        DriverManager.getConnection(url, "sa", "").use { connection ->
            connection.createStatement().use { statement ->
                statement.executeQuery("SELECT 1").use { rs ->
                    rs.next()
                    assertEquals(1, rs.getInt(1))
                }
            }
        }
    }
}
