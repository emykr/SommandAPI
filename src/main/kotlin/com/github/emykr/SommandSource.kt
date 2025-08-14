package com.github.emykr

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * Abstraction of a command sender so higher level logic is not tightly coupled to Bukkit API.
 */
interface SommandSource {
    val sender: CommandSender

    /**
     * True when the underlying sender is a Player.
     */
    val isPlayer: Boolean
        get() = sender is Player

    /**
     * Returns the Player or null when the sender is not a player.
     */
    fun asPlayer(): Player? = sender as? Player

    /**
     * Sends a plain text message to the sender.
     */
    fun send(message: String)
}

/**
 * Default implementation that simply wraps a Bukkit CommandSender.
 */
class BukkitSommandSource(override val sender: CommandSender) : SommandSource {
    override fun send(message: String) {
        sender.sendMessage(message)
    }

}