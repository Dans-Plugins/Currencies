package com.dansplugins.currencies.command

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import preponderous.ponder.command.unquote

class ArgsTest {

    /**
     * Pins the upstream behaviour the wrapper exists to guard against, so a Ponder release that
     * fixes it is noticed here rather than silently making the wrapper redundant.
     */
    @Test
    fun `ponder's unquote still crashes on an empty or unbalanced quote`() {
        assertThrows(StringIndexOutOfBoundsException::class.java) { arrayOf("\"\"").unquote() }
        assertThrows(StringIndexOutOfBoundsException::class.java) { arrayOf("\"").unquote() }
        // A lone empty string, by contrast, survives unquoting as itself.
        assertEquals(listOf(""), arrayOf("").unquote().toList())
    }

    @Test
    fun `a quoted multi-word argument is joined`() {
        assertEquals(listOf("Gold Coin"), arrayOf("\"Gold", "Coin\"").unquoteSafely())
        assertEquals(listOf("Gold Coin", "confirm"), arrayOf("\"Gold", "Coin\"", "confirm").unquoteSafely())
        assertEquals(listOf("a", "b"), arrayOf("\"a\"", "\"b\"").unquoteSafely())
    }

    @Test
    fun `unquoted arguments are passed through`() {
        assertEquals(listOf("Gold", "Coin"), arrayOf("Gold", "Coin").unquoteSafely())
        assertEquals(emptyList<String>(), emptyArray<String>().unquoteSafely())
    }

    @Test
    fun `an empty pair of quotes falls back to the raw arguments`() {
        assertEquals(listOf("\"\""), arrayOf("\"\"").unquoteSafely())
        assertEquals(listOf("Gold", "\"\""), arrayOf("Gold", "\"\"").unquoteSafely())
    }

    @Test
    fun `an unbalanced quote falls back to the raw arguments`() {
        assertEquals(listOf("\""), arrayOf("\"").unquoteSafely())
        assertEquals(listOf("\"", "Gold"), arrayOf("\"", "Gold").unquoteSafely())
    }

    @Test
    fun `a lone empty string is passed through`() {
        assertEquals(listOf(""), arrayOf("").unquoteSafely())
    }
}
