package com.github.emykr.compat

import com.github.emykr.compat.v1_21_4.VersionCompat as Compat_1_21_4
import com.github.emykr.compat.v1_21_9.VersionCompat as Compat_1_21_9
import com.github.emykr.compat.v1_21_10.VersionCompat as Compat_1_21_10
import org.bukkit.Bukkit

/**
 * Resolves the appropriate BaseCompat for the running server version.
 *
 * It parses Bukkit version strings such as:
 * - "1.21.4-R0.1-SNAPSHOT"
 * - "git-Paper-123 (MC: 1.21.9)"
 */
object CompatResolver {

    /**
     * Returns a non-null BaseCompat suitable for the current server.
     */
    fun resolve(): BaseCompat {
        val versionString = extractServerVersion()
        return when (versionString) {
            "1.21.4" -> Compat_1_21_4()
            "1.21.9" -> Compat_1_21_9()
            "1.21.10" -> Compat_1_21_10()
            else -> FallbackCompat(versionString)
        }
    }

    /**
     * Extracts "major.minor.patch" (e.g., "1.21.4") from Bukkit's version strings.
     */
    private fun extractServerVersion(): String {
        val bukkitVersion = runCatching { Bukkit.getBukkitVersion() }.getOrNull()
        parseMcVersion(bukkitVersion)?.let { return it }

        val fullVersion = runCatching { Bukkit.getVersion() }.getOrNull()
        parseMcVersion(fullVersion)?.let { return it }

        return "unknown"
    }

    /**
     * Finds the first occurrence of "digits.digits.digits" in the source.
     */
    private fun parseMcVersion(source: String?): String? {
        if (source.isNullOrBlank()) {
            return null
        }
        val regex = Regex("""\d+\.\d+\.\d+""")
        return regex.find(source)?.value
    }

    /**
     * Fallback implementation when the server version is unknown or unsupported.
     */
    private class FallbackCompat(
        override val serverVersion: String
    ) : BaseCompat()
}