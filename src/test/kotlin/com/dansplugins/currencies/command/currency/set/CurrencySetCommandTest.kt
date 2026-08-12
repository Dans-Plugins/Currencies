package com.dansplugins.currencies.command.currency.set

import com.dansplugins.currencies.Currencies
import com.dansplugins.currencies.currency.Currency
import com.dansplugins.factionsystem.faction.MfFactionId
import io.mockk.every
import io.mockk.mockk
import org.bukkit.ChatColor.RED
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CurrencySetCommandTest {

    private val gold = Currency(factionId = MfFactionId("faction"), name = "Gold", item = mockk(relaxed = true))
    private val plugin = mockk<Currencies>(relaxed = true) {
        every { name } returns "currencies"
        every { services.currencyService.currencies } returns listOf(gold)
    }
    private val bukkitCommand = mockk<Command>(relaxed = true)
    private val setCommand = CurrencySetCommand(plugin)

    private fun messagesSentBy(vararg args: String): List<String> {
        val messages = mutableListOf<String>()
        val sender = mockk<CommandSender>(relaxed = true)
        every { sender.sendMessage(capture(messages)) } returns Unit
        assertTrue(setCommand.onCommand(sender, bukkitCommand, "currency", args))
        return messages
    }

    private fun tabCompletionFor(vararg args: String) =
        setCommand.onTabComplete(mockk(relaxed = true), bukkitCommand, "currency", args)

    @Test
    fun `usage message is sent when no subcommand is given`() {
        assertEquals(
            listOf("${RED}Usage: /currency set [name|desc]"),
            messagesSentBy()
        )
    }

    @Test
    fun `usage message is sent when the subcommand is not recognised`() {
        assertEquals(
            listOf("${RED}Usage: /currency set [name|desc]"),
            messagesSentBy("notasubcommand")
        )
    }

    @Test
    fun `name is routed to the set name command`() {
        assertEquals(
            listOf("${RED}You do not have permission to rename currencies."),
            messagesSentBy("name", "Gold", "Silver")
        )
    }

    @Test
    fun `description is routed to the set description command`() {
        assertEquals(
            listOf("${RED}You do not have permission to change the description of currencies."),
            messagesSentBy("description", "Gold", "Shiny")
        )
    }

    @Test
    fun `desc is an alias of description`() {
        assertEquals(
            listOf("${RED}You do not have permission to change the description of currencies."),
            messagesSentBy("desc", "Gold", "Shiny")
        )
    }

    @Test
    fun `subcommands are matched regardless of case`() {
        assertEquals(
            listOf("${RED}You do not have permission to rename currencies."),
            messagesSentBy("NAME", "Gold", "Silver")
        )
    }

    @Test
    fun `every advertised subcommand is offered by tab completion`() {
        assertEquals(listOf("name", "description", "desc"), tabCompletionFor())
    }

    @Test
    fun `tab completion filters the subcommands by the partial argument`() {
        assertEquals(listOf("description", "desc"), tabCompletionFor("d"))
        assertEquals(listOf("name"), tabCompletionFor("n"))
    }

    @Test
    fun `tab completion of later arguments is delegated to the routed subcommand`() {
        assertEquals(listOf("Gold"), tabCompletionFor("name", "Go"))
        assertEquals(listOf("Gold"), tabCompletionFor("desc", "Go"))
    }

    @Test
    fun `tab completion of later arguments is empty when the subcommand is not recognised`() {
        assertEquals(emptyList<String>(), tabCompletionFor("notasubcommand", "Go"))
    }
}
