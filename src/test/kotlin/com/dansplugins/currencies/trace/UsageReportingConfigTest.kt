package com.dansplugins.currencies.trace

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.bukkit.configuration.Configuration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Pins the one-argument getters. A config.yml written before the usage-reporting
 * block existed only receives the bundled key through the one-argument getters,
 * which fall through to the jar's defaults; the two-argument getters would return
 * their explicit fallback and silently turn reporting off on every upgrade.
 */
class UsageReportingConfigTest {

    private val config = mockk<Configuration>()

    @Test
    fun `settings are read with the one-argument getters so bundled defaults apply`() {
        every { config.getBoolean("usage-reporting.enabled") } returns true
        every { config.getString("usage-reporting.endpoint") } returns "https://trace.example.org"
        every { config.getString("usage-reporting.key") } returns "abc"

        val settings = UsageReportingConfig.read(config)

        assertTrue(settings.enabled)
        assertEquals("https://trace.example.org", settings.endpoint)
        assertEquals("abc", settings.key)
        verify(exactly = 0) { config.getBoolean("usage-reporting.enabled", any()) }
        verify(exactly = 0) { config.getString("usage-reporting.endpoint", any()) }
        verify(exactly = 0) { config.getString("usage-reporting.key", any()) }
    }

    @Test
    fun `a missing endpoint falls back to the author's trace server`() {
        every { config.getBoolean("usage-reporting.enabled") } returns true
        every { config.getString("usage-reporting.endpoint") } returns null
        every { config.getString("usage-reporting.key") } returns "abc"

        assertEquals("https://trace.danielstephenson.dev", UsageReportingConfig.read(config).endpoint)
    }

    @Test
    fun `a missing key is empty, which the client treats as off`() {
        every { config.getBoolean("usage-reporting.enabled") } returns true
        every { config.getString("usage-reporting.endpoint") } returns "https://trace.example.org"
        every { config.getString("usage-reporting.key") } returns null

        assertEquals("", UsageReportingConfig.read(config).key)
    }

    @Test
    fun `the switch is honoured`() {
        every { config.getBoolean("usage-reporting.enabled") } returns false
        every { config.getString("usage-reporting.endpoint") } returns "https://trace.example.org"
        every { config.getString("usage-reporting.key") } returns "abc"

        assertFalse(UsageReportingConfig.read(config).enabled)
    }
}
