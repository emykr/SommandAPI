package com.github.emykr.util

import org.bukkit.OfflinePlayer
import org.bukkit.World
import org.bukkit.command.CommandSender
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Utility for converting numbers to strings in a consistent way across the plugin.
 *
 * This class centralizes:
 * - Basic number → string conversion
 * - Locale-aware formatting
 * - Custom pattern formatting
 * - Common Bukkit-related message patterns that contain numbers
 *
 * All methods are null-safe and avoid throwing NPEs.
 */
object NumberStrings {

    // region: Basic conversion

    /**
     * Converts a nullable [Number] to a string.
     *
     * - When [value] is null, [nullText] is returned (default: "-").
     * - Uses [String.valueOf] which handles all primitive wrappers.
     */
    @JvmStatic
    @JvmOverloads
    fun basic(
        value: Number?,
        nullText: String = "-"
    ): String {
        // Fast null handling to avoid NPE.
        val number: Number = value ?: return nullText
        return java.lang.String.valueOf(number)
    }

    /**
     * Converts a nullable [Number] to a string with locale-aware formatting.
     *
     * Example:
     * - locale=Locale.US, value=12345.67 → "12,345.67"
     * - locale=Locale.KOREA, value=12345.67 → "12,345.67" (same grouping, different symbols)
     *
     * When [value] is null, [nullText] is returned (default: "-").
     */
    @JvmStatic
    @JvmOverloads
    fun withLocale(
        value: Number?,
        locale: Locale = Locale.getDefault(),
        nullText: String = "-"
    ): String {
        val number: Number = value ?: return nullText
        // Use DecimalFormat with locale-aware symbols.
        val symbols = DecimalFormatSymbols.getInstance(locale)
        val format = DecimalFormat("#,##0.###", symbols)
        return format.format(number)
    }

    /**
     * Converts a nullable [Number] using a custom decimal pattern.
     *
     * @param pattern DecimalFormat pattern, e.g. "#,##0", "0.00", "#,##0.000".
     * @param locale Locale to use for symbols (decimal/group separators).
     * @param nullText Fallback text when [value] is null.
     */
    @JvmStatic
    @JvmOverloads
    fun withPattern(
        value: Number?,
        pattern: String,
        locale: Locale = Locale.getDefault(),
        nullText: String = "-"
    ): String {
        require(pattern.isNotBlank()) { "Pattern must not be blank." }
        val number: Number = value ?: return nullText
        val symbols = DecimalFormatSymbols.getInstance(locale)
        val format = DecimalFormat(pattern, symbols)
        return format.format(number)
    }

    // endregion

    // region: Prefix / suffix / template helpers

    /**
     * Returns "[prefix][number]" with basic conversion.
     *
     * null → uses [nullText] instead of the number part.
     */
    @JvmStatic
    @JvmOverloads
    fun withPrefix(
        value: Number?,
        prefix: String,
        nullText: String = "-"
    ): String {
        val numText = basic(value, nullText)
        return prefix + numText
    }

    /**
     * Returns "[number][suffix]" with basic conversion.
     *
     * null → uses [nullText] instead of the number part.
     */
    @JvmStatic
    @JvmOverloads
    fun withSuffix(
        value: Number?,
        suffix: String,
        nullText: String = "-"
    ): String {
        val numText = basic(value, nullText)
        return numText + suffix
    }

    /**
     * Simple template replacement.
     *
     * Example:
     *  template = "You have {amount} coins."
     *  placeholder = "{amount}"
     *  value = 10
     *  → "You have 10 coins."
     */
    @JvmStatic
    @JvmOverloads
    fun template(
        template: String,
        placeholder: String,
        value: Number?,
        nullText: String = "-"
    ): String {
        require(template.isNotEmpty()) { "Template must not be empty." }
        require(placeholder.isNotEmpty()) { "Placeholder must not be empty." }

        val numText = basic(value, nullText)
        return template.replace(placeholder, numText)
    }

    // endregion

    // region: Color code helpers (Bukkit style)

    /**
     * Converts '&' color codes to '§' codes (commonly used by Bukkit).
     *
     * Example:
     *  "&aYou have 10 coins" → "§aYou have 10 coins"
     */
    @JvmStatic
    fun ampersandToSection(text: String?): String {
        if (text.isNullOrEmpty()) return ""
        return text.replace('&', '§')
    }

    /**
     * Converts '§' color codes to '&' codes.
     */
    @JvmStatic
    fun sectionToAmpersand(text: String?): String {
        if (text.isNullOrEmpty()) return ""
        return text.replace('§', '&')
    }

    /**
     * Removes all Bukkit-style color codes (&x or §x).
     */
    @JvmStatic
    fun stripColors(text: String?): String {
        if (text.isNullOrEmpty()) return ""
        return text.replace(Regex("(?i)[§&][0-9A-FK-OR]"), "")
    }

    // endregion

    // region: Bukkit-specific message helpers

    /**
     * Creates a message describing a numeric value for a [CommandSender].
     *
     * Example: "Player Steve has 5 lives."
     */
    @JvmStatic
    fun forSenderValue(
        sender: CommandSender?,
        value: Number?,
        label: String = "value",
        locale: Locale = Locale.getDefault()
    ): String {
        val name = sender?.name ?: "unknown"
        val numText = withLocale(value, locale, nullText = "-")
        return "Sender $name has $label: $numText"
    }

    /**
     * Creates a message describing a numeric value for a [Player].
     *
     * Example: "Player Steve has 10 points."
     */
    @JvmStatic
    fun forPlayerValue(
        player: Player?,
        value: Number?,
        label: String = "value",
        locale: Locale = Locale.getDefault()
    ): String {
        val name = player?.name ?: "unknown"
        val numText = withLocale(value, locale, nullText = "-")
        return "Player $name has $label: $numText"
    }

    /**
     * Creates a message for an [OfflinePlayer] with a numeric value.
     */
    @JvmStatic
    fun forOfflinePlayerValue(
        player: OfflinePlayer?,
        value: Number?,
        label: String = "value",
        locale: Locale = Locale.getDefault()
    ): String {
        val name = player?.name ?: "unknown"
        val numText = withLocale(value, locale, nullText = "-")
        return "Offline player $name has $label: $numText"
    }

    /**
     * Creates a message for a [World] with a numeric value.
     *
     * Example: "World world_nether has difficulty level: 3"
     */
    @JvmStatic
    fun forWorldValue(
        world: World?,
        value: Number?,
        label: String = "value",
        locale: Locale = Locale.getDefault()
    ): String {
        val name = world?.name ?: "unknown"
        val numText = withLocale(value, locale, nullText = "-")
        return "World $name has $label: $numText"
    }

    /**
     * Creates a message for an [Entity] with a numeric value.
     *
     * Example: "Entity ZOMBIE(123) has health: 20.0"
     */
    @JvmStatic
    fun forEntityValue(
        entity: Entity?,
        value: Number?,
        label: String = "value",
        locale: Locale = Locale.getDefault()
    ): String {
        if (entity == null) {
            val numText = withLocale(value, locale, nullText = "-")
            return "Entity <null> has $label: $numText"
        }

        val typeName = entity.type.name
        val idPart = entity.entityId
        val numText = withLocale(value, locale, nullText = "-")
        return "Entity $typeName($idPart) has $label: $numText"
    }

    // endregion
}