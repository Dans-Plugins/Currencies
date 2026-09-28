package com.dansplugins.currencies.item

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.bukkit.Bukkit
import org.bukkit.Server
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ItemStacksTest {

    @AfterEach
    fun tearDown() {
        unmockkStatic(Bukkit::class)
    }

    /**
     * Paper's server class lives in an unversioned package, which made `/currency info` throw
     * StringIndexOutOfBoundsException before it printed the item and the minted total. A server
     * whose version cannot be read must answer null, so the caller prints the plain item name.
     */
    @Test
    fun `a server without a versioned CraftBukkit package answers null instead of throwing`() {
        mockkStatic(Bukkit::class)
        every { Bukkit.getServer() } returns mockk<Server>(relaxed = true)
        assertNull(mockk<ItemStack>(relaxed = true).tagToNbtJson())
    }
}
