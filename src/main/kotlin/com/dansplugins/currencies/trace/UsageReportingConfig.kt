package com.dansplugins.currencies.trace

import org.bukkit.configuration.Configuration

/**
 * The `usage-reporting` block of `config.yml`, read the way that survives an
 * upgrade from a version before the block existed.
 */
data class UsageReportingConfig(
    val enabled: Boolean,
    val endpoint: String,
    /** Empty when no key is configured or bundled, which the client treats as "off". */
    val key: String
) {
    companion object {
        const val ENABLED_KEY = "usage-reporting.enabled"
        const val ENDPOINT_KEY = "usage-reporting.endpoint"
        const val KEY_KEY = "usage-reporting.key"
        const val DEFAULT_ENDPOINT = "https://trace.danielstephenson.dev"

        // The one-argument getters, deliberately. saveDefaultConfig() never touches a
        // config.yml that already exists, so a server upgraded from a version before
        // usage reporting has no usage-reporting block on disk. Bukkit registers the
        // jar's config.yml as the defaults for that file, and the one-argument
        // getters fall through to them -- but the two-argument getters return their
        // explicit fallback instead, which for the key would be "" and would turn
        // reporting off on every existing installation. Verified against
        // YamlConfiguration, not assumed.
        fun read(config: Configuration): UsageReportingConfig = UsageReportingConfig(
            enabled = config.getBoolean(ENABLED_KEY),
            endpoint = config.getString(ENDPOINT_KEY) ?: DEFAULT_ENDPOINT,
            key = config.getString(KEY_KEY) ?: ""
        )
    }
}
