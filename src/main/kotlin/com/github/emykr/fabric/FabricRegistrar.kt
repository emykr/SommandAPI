package com.github.emykr.fabric

import com.github.emykr.fabric.brigadier.BrigadierRegistrar
import com.github.emykr.SommandSource
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback

class FabricSommandSource(private val source: ServerCommandSource) : SommandSource {
    override fun hasPermission(permission: String): Boolean {
        // Prefer server's permission check if available, otherwise fall back to op-level check via reflection
        return try {
            // Some implementations expose hasPermissionLevel(int)
            val method = source.javaClass.getMethod("hasPermissionLevel", Int::class.javaPrimitiveType)
            val res = method.invoke(source, 2) as? Boolean
            res ?: true
        } catch (_: Throwable) {
            true
        }
    }

    override val isPlayer: Boolean
        get() = try {
            // try to get player property via reflection
            val playerField = source.javaClass.getMethod("getPlayer")
            playerField.invoke(source) != null
        } catch (_: Throwable) {
            try {
                val getEntity = source.javaClass.getMethod("getEntity")
                getEntity.invoke(source) != null
            } catch (_: Throwable) {
                false
            }
        }

    override fun asPlayer(): Any? = try {
        source.javaClass.getMethod("getPlayer").invoke(source)
    } catch (_: Throwable) {
        try {
            source.javaClass.getMethod("getEntity").invoke(source)
        } catch (_: Throwable) {
            null
        }
    }

    override fun send(message: String) {
        try {
            // Try Text.literal(message) then call sendFeedback
            val textClass = try {
                Class.forName("net.minecraft.text.Text")
            } catch (_: Throwable) {
                null
            }
            val textInstance = try {
                textClass?.getMethod("literal", CharSequence::class.java)?.invoke(null, message)
            } catch (_: Throwable) {
                try {
                    textClass?.getMethod("of", CharSequence::class.java)?.invoke(null, message)
                } catch (_: Throwable) {
                    null
                }
            }

            if (textInstance != null) {
                try {
                    val sendFeedback = source.javaClass.getMethod("sendFeedback", textClass, Boolean::class.javaPrimitiveType)
                    sendFeedback.invoke(source, textInstance, false)
                    return
                } catch (_: Throwable) {
                    // fallback
                }
            }

            // Fallback: try sendSystemMessage(String) if present
            try {
                val sendSystem = source.javaClass.getMethod("sendSystemMessage", CharSequence::class.java)
                sendSystem.invoke(source, message)
                return
            } catch (_: Throwable) {
                // ignore
            }
        } catch (_: Throwable) {
            // ignore
        }
    }
}

class FabricRegistrar : ModInitializer {
    override fun onInitialize() {
        // Register commands during command registration. Use explicit generic to help type inference.
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            BrigadierRegistrar.registerAll<ServerCommandSource>(dispatcher, ::FabricSommandSource)
        }
    }
}
