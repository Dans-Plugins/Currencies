package com.dansplugins.currencies

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.sql.DriverManager

class CurrenciesJdbcUrlTest {

    private val defaultUrl = "jdbc:h2:./medieval_factions_db;AUTO_SERVER=true;MODE=MYSQL;DATABASE_TO_UPPER=false"

    @Test
    fun hardenJdbcUrl_leavesTheDefaultAutoServerUrlAlone() {
        // H2 2.1.214 refuses AUTO_SERVER=TRUE together with DB_CLOSE_ON_EXIT=FALSE (50100).
        assertEquals(defaultUrl, Currencies.hardenJdbcUrl(defaultUrl))
        assertEquals("jdbc:h2:./db;auto_server = true", Currencies.hardenJdbcUrl("jdbc:h2:./db;auto_server = true"))
    }

    @Test
    fun hardenJdbcUrl_appendsCloseOnExitFalse_toAnEmbeddedH2Url() {
        val embedded = "jdbc:h2:./medieval_factions_db;MODE=MYSQL;DATABASE_TO_UPPER=false"
        assertEquals("$embedded;DB_CLOSE_ON_EXIT=FALSE", Currencies.hardenJdbcUrl(embedded))
        assertEquals("jdbc:h2:./db;AUTO_SERVER=FALSE;DB_CLOSE_ON_EXIT=FALSE", Currencies.hardenJdbcUrl("jdbc:h2:./db;AUTO_SERVER=FALSE"))
    }

    @Test
    fun hardenJdbcUrl_producesUrlsTheBundledH2Opens_onFileStores() {
        val dir = java.nio.file.Files.createTempDirectory("currencies-jdbc").toFile()
        try {
            val urls = listOf(
                Currencies.hardenJdbcUrl("jdbc:h2:$dir/auto;AUTO_SERVER=true;MODE=MYSQL;DATABASE_TO_UPPER=false"),
                Currencies.hardenJdbcUrl("jdbc:h2:$dir/embedded;MODE=MYSQL;DATABASE_TO_UPPER=false")
            )
            for (url in urls) {
                DriverManager.getConnection(url, "sa", "").use { connection ->
                    connection.createStatement().use { statement ->
                        statement.executeQuery("SELECT 1").use { rs ->
                            rs.next()
                            assertEquals(1, rs.getInt(1))
                        }
                    }
                }
            }
        } finally {
            dir.deleteRecursively()
        }
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
