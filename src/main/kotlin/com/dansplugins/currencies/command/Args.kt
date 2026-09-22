package com.dansplugins.currencies.command

import preponderous.ponder.command.unquote

/**
 * Strips the double quotes Bukkit leaves on a multi-word argument, the way Ponder's [unquote] does,
 * but never lets a player's input crash the command.
 *
 * Ponder's [unquote] throws [StringIndexOutOfBoundsException] on an argument that is an empty or
 * unbalanced pair of quotes (`""` or `"`), which reached the player as "An internal error occurred".
 * This falls back to the arguments as given instead. It also guarantees a non-empty array never
 * unquotes to nothing, since the callers that check `args.isEmpty()` *before* unquoting index
 * `[0]` straight afterwards.
 */
fun Array<out String>.unquoteSafely(): List<String> {
    val unquoted = try {
        unquote().toList()
    } catch (e: StringIndexOutOfBoundsException) {
        return toList()
    }
    return if (unquoted.isEmpty() && isNotEmpty()) toList() else unquoted
}
