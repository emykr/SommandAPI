package com.github.emykr.compat

import com.github.emykr.SommandDispatcher
import com.github.emykr.impl.DefaultDispatcher

/**
 * BaseCompat hides server-version-specific differences for SommandAPI.
 *
 * Each concrete implementation represents a specific server version and can
 * override dispatcher creation and user-facing messages if needed.
 */
abstract class BaseCompat {

    /**
     * Server version represented by this compat implementation.
     * Example: "1.21.4", "1.21.9", "1.21.10", or "unknown".
     */
    abstract val serverVersion: String

    /**
     * Creates a dispatcher appropriate for this server version.
     *
     * Default implementation uses the shared DefaultDispatcher.
     */
    open fun createDispatcher(): SommandDispatcher {
        return DefaultDispatcher()
    }

    /**
     * Default message when an unknown subcommand is entered.
     */
    open fun unknownSubcommandMessage(rootLabel: String): String {
        return "Unknown subcommand. Use /$rootLabel help"
    }

    /**
     * Default message when the sender lacks permission.
     */
    open fun noPermissionMessage(): String {
        return "You do not have permission."
    }

    /**
     * Creates a message for argument parse failures.
     */
    open fun argumentParseFailedMessage(reason: String?): String {
        val safeReason = reason?.takeIf { it.isNotBlank() } ?: "Invalid argument."
        return "Failed to parse argument: $safeReason"
    }

    /**
     * Message shown when an unexpected error happens during command execution.
     */
    open fun executionErrorMessage(): String {
        return "An internal error occurred while attempting to perform this command."
    }
}