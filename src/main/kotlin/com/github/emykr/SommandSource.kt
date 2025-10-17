package com.github.emykr

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * Abstraction of a command sender so higher level logic is not tightly coupled to a specific server API.
 */
interface SommandSource {
    /**
     * Returns true when the sender has the given permission.
     */
    fun hasPermission(permission: String): Boolean

    /**
     * True when the underlying sender is a Player.
     */
    val isPlayer: Boolean

    /**
     * Returns the platform-specific player object or null when not a player.
     */
    fun asPlayer(): Any?

    /**
     * Sends a plain text message to the sender.
     */
    fun send(message: String)
}

/**
 * Bukkit implementation that wraps a Bukkit CommandSender.
 */
class BukkitSommandSource(private val sender: CommandSender) : SommandSource {
    override fun hasPermission(permission: String): Boolean = sender.hasPermission(permission)

    override val isPlayer: Boolean
        get() = sender is Player

    override fun asPlayer(): Any? = sender as? Player

    override fun send(message: String) {
        sender.sendMessage(message)
    }

}