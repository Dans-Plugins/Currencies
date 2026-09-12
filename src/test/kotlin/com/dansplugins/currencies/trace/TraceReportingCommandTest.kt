package com.dansplugins.currencies.trace

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TraceReportingCommandTest {

    private interface Delegate : CommandExecutor, TabCompleter

    private val trace = mockk<TraceClient>(relaxed = true)
    private val delegate = mockk<Delegate>(relaxed = true)
    private val sender = mockk<CommandSender>(relaxed = true)
    private val bukkitCommand = mockk<Command>(relaxed = true) {
        every { name } returns "currency"
    }
    private val command = TraceReportingCommand(trace, delegate)

    @Test
    fun `each use is reported by command name only, then handled by the delegate`() {
        val args = arrayOf("balance", "GoldCoin")
        every { delegate.onCommand(sender, bukkitCommand, "currencies", args) } returns true

        assertTrue(command.onCommand(sender, bukkitCommand, "currencies", args))

        verify(exactly = 1) { trace.report("command", null, mapOf("name" to "currency")) }
        verify(exactly = 1) { delegate.onCommand(sender, bukkitCommand, "currencies", args) }
    }

    @Test
    fun `tab completion is passed through and not reported`() {
        val args = arrayOf("bal")
        every { delegate.onTabComplete(sender, bukkitCommand, "currency", args) } returns listOf("balance")

        assertEquals(listOf("balance"), command.onTabComplete(sender, bukkitCommand, "currency", args))

        verify(exactly = 0) { trace.report(any(), any(), any()) }
        verify(exactly = 0) { trace.report(any()) }
    }
}
