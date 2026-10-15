package com.dansplugins.currencies.command.currency.create

import com.dansplugins.currencies.Currencies
import com.dansplugins.currencies.currency.Currency
import com.dansplugins.currencies.currency.CurrencyId
import com.dansplugins.factionsystem.faction.MfFaction
import com.dansplugins.factionsystem.faction.MfFactionId
import com.dansplugins.factionsystem.faction.role.MfFactionRole
import com.dansplugins.factionsystem.failure.ServiceFailure
import com.dansplugins.factionsystem.failure.ServiceFailureType.GENERAL
import com.dansplugins.factionsystem.player.MfPlayerId
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.ChatColor.GREEN
import org.bukkit.ChatColor.RED
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class CurrencyCreateCommandTest {

    private val plugin = mockk<Currencies>(relaxed = true) {
        every { name } returns "currencies"
    }
    private val currencyService = plugin.services.currencyService
    private val factionService = plugin.medievalFactions.services.factionService
    private val scheduler = plugin.server.scheduler
    private val bukkitCommand = mockk<Command>(relaxed = true)
    private val createCommand = CurrencyCreateCommand(plugin)

    private val gold = Currency(id = CurrencyId("gold"), factionId = MfFactionId("faction"), name = "Gold", item = mockk(relaxed = true))

    /**
     * The command copies the held item into a real ItemStack, whose meta handling goes through
     * Bukkit's static item factory, so the static server is pointed at the plugin's mocked one.
     * The meta's clone is itself, so a display name set on it is visible on the copy.
     */
    private val itemMeta = mockk<ItemMeta>(relaxed = true)

    @BeforeEach
    fun setUp() {
        mockkStatic(Bukkit::class)
        every { Bukkit.getServer() } returns plugin.server
        every { Bukkit.getItemFactory() } returns plugin.server.itemFactory
        every { plugin.server.itemFactory.getItemMeta(any()) } returns itemMeta
        every { plugin.server.itemFactory.isApplicable(any<ItemMeta>(), any<Material>()) } returns true
        every { plugin.server.itemFactory.asMetaFor(any(), any<Material>()) } answers { firstArg() }
        every { plugin.server.itemFactory.equals(any(), any()) } returns false
        every { itemMeta.clone() } returns itemMeta
        every { scheduler.runTaskAsynchronously(plugin, any<Runnable>()) } answers {
            secondArg<Runnable>().run()
            mockk(relaxed = true)
        }
        every { currencyService.save(any()) } answers { Success(firstArg()) }
        every { factionService.save(any()) } answers { Success(firstArg()) }
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(Bukkit::class)
    }

    private fun heldItem(material: Material = Material.GOLD_INGOT, heldAmount: Int = 1, displayName: String? = null): ItemStack {
        every { itemMeta.hasDisplayName() } returns (displayName != null)
        every { itemMeta.displayName } returns (displayName ?: "")
        return mockk(relaxed = true) {
            every { type } returns material
            every { amount } returns heldAmount
            every { hasItemMeta() } returns false
        }
    }

    private fun player(permitted: Boolean = true, item: ItemStack = heldItem()) = mockk<Player>(relaxed = true) {
        every { hasPermission("currencies.create") } returns permitted
        every { uniqueId } returns UUID.randomUUID()
        every { inventory } returns mockk<PlayerInventory>(relaxed = true) {
            every { itemInMainHand } returns item
        }
    }

    private fun messagesSentBy(sender: CommandSender, vararg args: String): List<String> {
        val messages = mutableListOf<String>()
        every { sender.sendMessage(capture(messages)) } returns Unit
        assertTrue(createCommand.onCommand(sender, bukkitCommand, "create", args))
        return messages
    }

    private fun playerFaction(
        role: MfFactionRole? = mockk(relaxed = true) { every { hasPermission(any(), any()) } returns true }
    ): MfFaction {
        val faction = mockk<MfFaction>(relaxed = true) {
            every { id } returns MfFactionId("faction")
            every { getRole(any<MfPlayerId>()) } returns role
        }
        every { factionService.getFaction(any<MfPlayerId>()) } returns faction
        return faction
    }

    private fun currencyNameTaken(vararg currencies: Currency) {
        every { currencyService.getCurrency(any<String>()) } answers { currencies.singleOrNull { it.name.equals(firstArg<String>(), ignoreCase = true) } }
    }

    private fun savedCurrency(): Currency {
        val saved = slot<Currency>()
        verify { currencyService.save(capture(saved)) }
        return saved.captured
    }

    @Test
    fun `senders without the create permission are refused`() {
        assertEquals(
            listOf("${RED}You do not have permission to create currencies."),
            messagesSentBy(player(permitted = false), "Gold")
        )
        verify(exactly = 0) { currencyService.save(any()) }
    }

    @Test
    fun `non-players are refused even when permitted`() {
        val console = mockk<CommandSender>(relaxed = true) {
            every { hasPermission("currencies.create") } returns true
        }
        assertEquals(
            listOf("${RED}You must be a player to perform this command."),
            messagesSentBy(console, "Gold")
        )
    }

    @Test
    fun `usage message is sent when no currency name is given`() {
        assertEquals(
            listOf("${RED}Usage: /currency create [currency name]"),
            messagesSentBy(player())
        )
    }

    @Test
    fun `an empty hand is refused`() {
        assertEquals(
            listOf("${RED}You must hold the item you wish to use for this currency."),
            messagesSentBy(player(item = heldItem(material = Material.AIR)), "Gold")
        )
        assertEquals(
            listOf("${RED}You must hold the item you wish to use for this currency."),
            messagesSentBy(player(item = heldItem(heldAmount = 0)), "Gold")
        )
        verify(exactly = 0) { currencyService.save(any()) }
    }

    /**
     * Without --rename or --no-rename, a display name that differs from the currency name (including
     * no display name at all) asks the player which to use instead of creating the currency.
     */
    @Test
    fun `a display name that differs from the currency name prompts for a choice`() {
        val sender = player(item = heldItem(displayName = "Shiny Ingot"))
        val components = mutableListOf<BaseComponent>()
        every { sender.spigot().sendMessage(*varargAll { components.add(it); true }) } returns Unit
        assertEquals(
            listOf("${RED}The item's display name does not match the specified currency name. Would you like to update it to match?"),
            messagesSentBy(sender, "Royal", "Gold")
        )
        val choices = components.filterIsInstance<TextComponent>().filter { it.clickEvent != null }
        assertEquals(listOf("Yes", "No"), choices.map { it.text })
        assertEquals(
            listOf("/currency create Royal Gold --rename", "/currency create Royal Gold --no-rename"),
            choices.map { it.clickEvent.value }
        )
        verify(exactly = 0) { scheduler.runTaskAsynchronously(plugin, any<Runnable>()) }
        verify(exactly = 0) { currencyService.save(any()) }
    }

    @Test
    fun `an item with no display name prompts for a choice`() {
        assertEquals(
            listOf("${RED}The item's display name does not match the specified currency name. Would you like to update it to match?"),
            messagesSentBy(player(item = heldItem(displayName = null)), "Gold")
        )
        verify(exactly = 0) { currencyService.save(any()) }
    }

    @Test
    fun `a display name matching the currency name once colours are stripped creates the currency`() {
        currencyNameTaken()
        playerFaction()
        assertEquals(
            listOf("${GREEN}Currency created."),
            messagesSentBy(player(item = heldItem(displayName = "${GREEN}Gold")), "Gold")
        )
        val saved = savedCurrency()
        assertEquals("Gold", saved.name)
        assertEquals(MfFactionId("faction"), saved.factionId)
        assertEquals(1, saved.item.amount)
    }

    @Test
    fun `--rename sets the item's display name to the currency name`() {
        currencyNameTaken()
        playerFaction()
        assertEquals(
            listOf("${GREEN}Currency created."),
            messagesSentBy(player(item = heldItem(displayName = "Shiny Ingot")), "Royal", "Gold", "--rename")
        )
        assertEquals("Royal Gold", savedCurrency().name)
        verify { itemMeta.setDisplayName("Royal Gold") }
    }

    @Test
    fun `--no-rename keeps the item's display name`() {
        currencyNameTaken()
        playerFaction()
        assertEquals(
            listOf("${GREEN}Currency created."),
            messagesSentBy(player(item = heldItem(displayName = "Shiny Ingot")), "Royal", "Gold", "--no-rename")
        )
        assertEquals("Royal Gold", savedCurrency().name)
        verify(exactly = 0) { itemMeta.setDisplayName(any()) }
    }

    @Test
    fun `the currency id is stamped onto the currency item`() {
        currencyNameTaken()
        playerFaction()
        messagesSentBy(player(), "Gold", "--no-rename")
        val saved = savedCurrency()
        verify { itemMeta.persistentDataContainer.set(any(), any<PersistentDataType<String, String>>(), saved.id.value) }
    }

    @Test
    fun `a player in no faction is refused`() {
        currencyNameTaken()
        every { factionService.getFaction(any<MfPlayerId>()) } returns null
        assertEquals(
            listOf("${RED}You must be in a faction to create a currency."),
            messagesSentBy(player(), "Gold", "--no-rename")
        )
        verify(exactly = 0) { currencyService.save(any()) }
    }

    @Test
    fun `a player with no role in their faction is refused`() {
        currencyNameTaken()
        playerFaction(role = null)
        assertEquals(
            listOf("${RED}Your role in this faction does not give you permission to create currencies."),
            messagesSentBy(player(), "Gold", "--no-rename")
        )
        verify(exactly = 0) { currencyService.save(any()) }
    }

    @Test
    fun `a player whose role lacks the create-currency permission is refused`() {
        currencyNameTaken()
        playerFaction(role = mockk(relaxed = true) { every { hasPermission(any(), any()) } returns false })
        assertEquals(
            listOf("${RED}Your role in this faction does not give you permission to create currencies."),
            messagesSentBy(player(), "Gold", "--no-rename")
        )
        verify(exactly = 0) { currencyService.save(any()) }
    }

    @Test
    fun `a name already taken by another currency is refused`() {
        currencyNameTaken(gold)
        playerFaction()
        assertEquals(
            listOf("${RED}There is already a currency with that name."),
            messagesSentBy(player(), "gold", "--no-rename")
        )
        verify(exactly = 0) { currencyService.save(any()) }
        verify(exactly = 0) { factionService.save(any()) }
    }

    @Test
    fun `creating a currency saves the owning faction's new permissions`() {
        currencyNameTaken()
        playerFaction()
        messagesSentBy(player(), "Gold", "--no-rename")
        verify(exactly = 1) { factionService.save(any()) }
    }

    @Test
    fun `a failure to save the currency leaves the faction unsaved`() {
        currencyNameTaken()
        playerFaction()
        every { currencyService.save(any()) } returns Failure(ServiceFailure(GENERAL, "database unavailable", Exception("database unavailable")))
        assertEquals(
            listOf("${RED}Failed to save currency."),
            messagesSentBy(player(), "Gold", "--no-rename")
        )
        verify(exactly = 0) { factionService.save(any()) }
    }

    /**
     * Characterizes current behaviour: the currency has already been saved by the time the faction
     * save fails, so it is left in place without its per-currency faction permissions.
     */
    @Test
    fun `a failure to save the faction is reported after the currency was saved`() {
        currencyNameTaken()
        playerFaction()
        every { factionService.save(any()) } returns Failure(ServiceFailure(GENERAL, "database unavailable", Exception("database unavailable")))
        assertEquals(
            listOf("${RED}Failed to save faction."),
            messagesSentBy(player(), "Gold", "--no-rename")
        )
        verify(exactly = 1) { currencyService.save(any()) }
    }

    @Test
    fun `tab completion offers nothing`() {
        assertEquals(
            emptyList<String>(),
            createCommand.onTabComplete(mockk(relaxed = true), bukkitCommand, "create", arrayOf("G"))
        )
    }
}
