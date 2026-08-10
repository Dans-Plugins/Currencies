package com.dansplugins.currencies.command.currency

import com.dansplugins.currencies.Currencies
import io.mockk.every
import io.mockk.mockk
import org.bukkit.ChatColor.RED
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CurrencyCommandTest {

    private val plugin = mockk<Currencies>(relaxed = true) {
        every { name } returns "currencies"
    }
    private val bukkitCommand = mockk<Command>(relaxed = true)
    private val currencyCommand = CurrencyCommand(plugin)

    private fun messagesSentBy(vararg args: String): List<String> {
        val messages = mutableListOf<String>()
        val sender = mockk<CommandSender>(relaxed = true)
        every { sender.sendMessage(capture(messages)) } returns Unit
        assertTrue(currencyCommand.onCommand(sender, bukkitCommand, "currency", args))
        return messages
    }

    @Test
    fun `usage message advertises every subcommand offered by tab completion`() {
        val subcommands = currencyCommand.onTabComplete(mockk(relaxed = true), bukkitCommand, "currency", emptyArray())
        val usage = messagesSentBy("notasubcommand").single()
        subcommands.forEach { subcommand ->
            assertTrue(usage.contains(subcommand), "Usage message does not advertise '$subcommand': $usage")
        }
    }

    @Test
    fun `usage message is sent when no subcommand is given`() {
        assertEquals(
            listOf("${RED}Usage: /currency [balance|create|info|set|rename|list|mint|retire]"),
            messagesSentBy()
        )
    }

    @Test
    fun `rename is routed to the set name command rather than falling back to usage`() {
        assertEquals(
            listOf("${RED}You do not have permission to rename currencies."),
            messagesSentBy("rename", "GoldCoin", "SilverCoin")
        )
    }
}
