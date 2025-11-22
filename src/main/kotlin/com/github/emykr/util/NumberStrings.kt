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
 * Central numeric formatting utility (no extension functions).
 *
 * Provides:
 * - Basic / locale / pattern formatting
 * - Custom lambda formatting (wrapped safely)
 * - Template replacement
 * - Color code conversion
 * - Bukkit-related descriptive messages
 *
 * For more structured usage, see NumberFormatter interface and NumberFormatters object.
 */
object NumberStrings {

    /**
     * Basic null-safe conversion to string.
     */
    @JvmStatic
    @JvmOverloads
    fun basic(value: Number?, nullText: String = "-"): String {
        val number: Number = value ?: return nullText
        return java.lang.String.valueOf(number)
    }

    /**
     * Locale-aware formatting with default pattern "#,##0.###".
     */
    @JvmStatic
    @JvmOverloads
    fun withLocale(
        value: Number?,
        locale: Locale = Locale.getDefault(),
        nullText: String = "-"
    ): String {
        val number: Number = value ?: return nullText
        val symbols = DecimalFormatSymbols.getInstance(locale)
        val format = DecimalFormat("#,##0.###", symbols)
        return format.format(number)
    }

    /**
     * Pattern-based formatting.
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

    /**
     * Custom lambda formatting, safely wrapping null.
     */
    @JvmStatic
    @JvmOverloads
    fun custom(
        value: Number?,
        nullText: String = "-",
        formatter: (Number) -> String
    ): String {
        requireNotNull(formatter) { "Formatter lambda must not be null." }
        val number: Number = value ?: return nullText
        return formatter(number)
    }

    /**
     * Custom lambda with locale context.
     */
    @JvmStatic
    @JvmOverloads
    fun customWithLocale(
        value: Number?,
        locale: Locale = Locale.getDefault(),
        nullText: String = "-",
        formatter: (Number, Locale) -> String
    ): String {
        requireNotNull(formatter) { "Formatter lambda must not be null." }
        val number: Number = value ?: return nullText
        return formatter(number, locale)
    }

    /**
     * Prefix helper.
     */
    @JvmStatic
    @JvmOverloads
    fun withPrefix(
        value: Number?,
        prefix: String,
        nullText: String = "-"
    ): String {
        return prefix + basic(value, nullText)
    }

    /**
     * Suffix helper.
     */
    @JvmStatic
    @JvmOverloads
    fun withSuffix(
        value: Number?,
        suffix: String,
        nullText: String = "-"
    ): String {
        return basic(value, nullText) + suffix
    }

    /**
     * Single placeholder template replacement.
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
        val formatted = basic(value, nullText)
        return template.replace(placeholder, formatted)
    }

    // ---------------------------------------------------
    // Color code processing (Bukkit convention)
    // ---------------------------------------------------

    @JvmStatic
    fun ampersandToSection(text: String?): String {
        if (text.isNullOrEmpty()) return ""
        return text.replace('&', '§')
    }

    @JvmStatic
    fun sectionToAmpersand(text: String?): String {
        if (text.isNullOrEmpty()) return ""
        return text.replace('§', '&')
    }

    @JvmStatic
    fun stripColors(text: String?): String {
        if (text.isNullOrEmpty()) return ""
        return text.replace(Regex("(?i)[§&][0-9A-FK-OR]"), "")
    }

    // ---------------------------------------------------
    // Bukkit descriptive helpers
    // ---------------------------------------------------

    @JvmStatic
    fun forSenderValue(
        sender: CommandSender?,
        value: Number?,
        label: String = "value",
        locale: Locale = Locale.getDefault()
    ): String {
        val name = sender?.name ?: "unknown"
        val num = withLocale(value, locale)
        return "Sender $name has $label: $num"
    }

    @JvmStatic
    fun forPlayerValue(
        player: Player?,
        value: Number?,
        label: String = "value",
        locale: Locale = Locale.getDefault()
    ): String {
        val name = player?.name ?: "unknown"
        val num = withLocale(value, locale)
        return "Player $name has $label: $num"
    }

    @JvmStatic
    fun forOfflinePlayerValue(
        player: OfflinePlayer?,
        value: Number?,
        label: String = "value",
        locale: Locale = Locale.getDefault()
    ): String {
        val name = player?.name ?: "unknown"
        val num = withLocale(value, locale)
        return "Offline player $name has $label: $num"
    }

    @JvmStatic
    fun forWorldValue(
        world: World?,
        value: Number?,
        label: String = "value",
        locale: Locale = Locale.getDefault()
    ): String {
        val name = world?.name ?: "unknown"
        val num = withLocale(value, locale)
        return "World $name has $label: $num"
    }

    @JvmStatic
    fun forEntityValue(
        entity: Entity?,
        value: Number?,
        label: String = "value",
        locale: Locale = Locale.getDefault()
    ): String {
        if (entity == null) {
            val num = withLocale(value, locale)
            return "Entity <null> has $label: $num"
        }
        val type = entity.type.name
        val id = entity.entityId
        val num = withLocale(value, locale)
        return "Entity $type($id) has $label: $num"
    }
}