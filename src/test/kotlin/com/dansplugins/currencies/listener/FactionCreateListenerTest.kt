package com.dansplugins.currencies.listener

import com.dansplugins.currencies.Currencies
import com.dansplugins.factionsystem.event.faction.FactionCreateEvent
import com.dansplugins.factionsystem.faction.MfFaction
import com.dansplugins.factionsystem.faction.MfFactionId
import com.dansplugins.factionsystem.faction.permission.MfFactionPermission
import com.dansplugins.factionsystem.faction.role.MfFactionRole
import com.dansplugins.factionsystem.faction.role.MfFactionRoles
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FactionCreateListenerTest {

    private val plugin = mockk<Currencies>(relaxed = true)
    private val medievalFactionsPermissions = plugin.medievalFactions.factionPermissions
    private val listener = FactionCreateListener(plugin)

    private val changeName = MfFactionPermission("CHANGE_NAME", "Change name", false)
    private val createCurrency = MfFactionPermission("CREATE_CURRENCY", "Create currency", false)

    @BeforeEach
    fun setUp() {
        every { medievalFactionsPermissions.changeName } returns changeName
        every { plugin.factionPermissions.createCurrency } returns createCurrency
        every { medievalFactionsPermissions.setRolePermission(any()) } answers {
            val permission = firstArg<MfFactionPermission>()
            MfFactionPermission("SET_ROLE_PERMISSION(${permission.name})", "Set role permission", false)
        }
    }

    /**
     * A real role whose permission checks are answered from [granted], so the listener's
     * copy of it is a real role whose permissionsByName can be inspected.
     */
    private fun role(name: String, vararg granted: String, permissionsByName: Map<String, Boolean> = emptyMap()) =
        spyk(MfFactionRole(plugin = plugin.medievalFactions, name = name, permissionsByName = permissionsByName)) {
            every { hasPermission(any(), any()) } answers { secondArg<MfFactionPermission>().name in granted }
        }

    private fun create(vararg roles: MfFactionRole, defaultPermissionsByName: Map<String, Boolean> = emptyMap()): MfFaction {
        val faction = MfFaction(
            plugin = plugin.medievalFactions,
            name = "Faction",
            defaultPermissionsByName = defaultPermissionsByName,
            roles = MfFactionRoles(defaultRoleId = roles.first().id, roles = roles.toList())
        )
        val event = FactionCreateEvent(MfFactionId("faction"), faction, false)
        listener.onFactionCreate(event)
        return event.faction
    }

    @Test
    fun `create currency and its role permission default to denied for the faction`() {
        val faction = create(role("Member"), defaultPermissionsByName = mapOf("CHANGE_NAME" to false))
        assertEquals(
            mapOf(
                "CHANGE_NAME" to false,
                "SET_ROLE_PERMISSION(CREATE_CURRENCY)" to false,
                "CREATE_CURRENCY" to false
            ),
            faction.defaultPermissionsByName
        )
    }

    @Test
    fun `a role that can change the faction name can create currencies`() {
        val faction = create(role("Owner", "CHANGE_NAME"))
        assertEquals(mapOf("CREATE_CURRENCY" to true), faction.roles.single().permissionsByName)
    }

    @Test
    fun `a role that can grant changing the name can grant creating currencies`() {
        val faction = create(role("Officer", "SET_ROLE_PERMISSION(CHANGE_NAME)"))
        assertEquals(
            mapOf("SET_ROLE_PERMISSION(CREATE_CURRENCY)" to true),
            faction.roles.single().permissionsByName
        )
    }

    @Test
    fun `a role that can grant granting changing the name can grant granting creating currencies`() {
        val faction = create(role("Admin", "SET_ROLE_PERMISSION(SET_ROLE_PERMISSION(CHANGE_NAME))"))
        assertEquals(
            mapOf("SET_ROLE_PERMISSION(SET_ROLE_PERMISSION(CREATE_CURRENCY))" to true),
            faction.roles.single().permissionsByName
        )
    }

    @Test
    fun `a role with every name permission gets every currency permission`() {
        val faction = create(
            role(
                "Owner",
                "CHANGE_NAME",
                "SET_ROLE_PERMISSION(CHANGE_NAME)",
                "SET_ROLE_PERMISSION(SET_ROLE_PERMISSION(CHANGE_NAME))"
            )
        )
        assertEquals(
            mapOf(
                "SET_ROLE_PERMISSION(SET_ROLE_PERMISSION(CREATE_CURRENCY))" to true,
                "SET_ROLE_PERMISSION(CREATE_CURRENCY)" to true,
                "CREATE_CURRENCY" to true
            ),
            faction.roles.single().permissionsByName
        )
    }

    @Test
    fun `a role without name permissions is granted nothing and keeps its own permissions`() {
        val faction = create(role("Member", permissionsByName = mapOf("CLAIM" to true)))
        assertEquals(mapOf("CLAIM" to true), faction.roles.single().permissionsByName)
    }

    @Test
    fun `each role is granted only what its own name permissions allow`() {
        val faction = create(role("Owner", "CHANGE_NAME"), role("Member"))
        assertEquals(
            listOf(mapOf("CREATE_CURRENCY" to true), emptyMap()),
            faction.roles.map { it.permissionsByName }
        )
    }
}
