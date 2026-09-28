package com.dansplugins.currencies.item

import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

fun ItemStack.toByteArray(): ByteArray {
    ByteArrayOutputStream().use { byteArrayOutputStream ->
        BukkitObjectOutputStream(byteArrayOutputStream).use { bukkitObjectOutputStream ->
            bukkitObjectOutputStream.writeObject(this)
            return byteArrayOutputStream.toByteArray()
        }
    }
}

fun ByteArray.toItemStack(): ItemStack {
    ByteArrayInputStream(this).use { byteArrayInputStream ->
        BukkitObjectInputStream(byteArrayInputStream).use { bukkitObjectInputStream ->
            return bukkitObjectInputStream.readObject() as ItemStack
        }
    }
}

/**
 * The item's NBT tag as JSON, for a chat hover, or null when it cannot be read on this server.
 * The lookup is by server version and NMS method name, so it only covers the Spigot versions
 * listed below. Paper has not versioned the CraftBukkit package since 1.20.5; there the
 * package name has no version suffix and this answers null (the caller then prints the plain
 * item name) instead of throwing.
 */
fun ItemStack.tagToNbtJson(): String? = try {
    nbtJsonOrNull()
} catch (e: ReflectiveOperationException) {
    null
} catch (e: RuntimeException) {
    null
}

private fun ItemStack.nbtJsonOrNull(): String? {
    val serverVersion = Bukkit.getServer().javaClass.packageName
        .removePrefix("org.bukkit.craftbukkit")
        .removePrefix(".")
        .takeIf { it.isNotEmpty() && !it.contains('.') } ?: return null
    val craftItemStackClass = Class.forName("org.bukkit.craftbukkit.${serverVersion}.inventory.CraftItemStack")
    val asNmsCopyMethod = craftItemStackClass.getMethod("asNMSCopy", ItemStack::class.java)
    val nmsItemStackClass = Class.forName("net.minecraft.world.item.ItemStack")
    val nbtTagCompoundClass = Class.forName("net.minecraft.nbt.NBTTagCompound")
    val getTagMethodName = when (serverVersion) {
        "v1_17_R1" -> "s"
        "v1_18_R1" -> "s"
        "v1_18_R2" -> "t"
        "v1_19_R1" -> "u"
        else -> null
    } ?: return null
    val nmsGetTagMethod = nmsItemStackClass.getMethod(getTagMethodName)
    val nmsItemStackObj = asNmsCopyMethod(null, this)
    val itemTagAsJsonObject = nmsGetTagMethod(nmsItemStackObj) ?: nbtTagCompoundClass.getDeclaredConstructor().newInstance()
    return itemTagAsJsonObject.toString()
}