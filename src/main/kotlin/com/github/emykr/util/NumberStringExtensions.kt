package com.github.emykr.util

import org.bukkit.OfflinePlayer
import org.bukkit.World
import org.bukkit.command.CommandSender
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import java.util.Locale

/**
 * Extension helpers for numeric-to-string conversions and
 * common Bukkit message patterns.
 *
 * These are thin wrappers over [NumberStrings].
 */

// region Number extensions

/**
 * Shortcut for [NumberStrings.basic].
 */
fun Number?.toBasicString(nullText: String = "-"): String =
        NumberStrings.basic(this, nullText)

/**
 * Shortcut for [NumberStrings.withLocale].
 */
fun Number?.toLocalizedString(
    locale: Locale = Locale.getDefault(),
    nullText: String = "-"
): String = NumberStrings.withLocale(this, locale, nullText)

/**
 * Shortcut for [NumberStrings.withPattern].
 */
fun Number?.toPatternString(
    pattern: String,
    locale: Locale = Locale.getDefault(),
    nullText: String = "-"
): String = NumberStrings.withPattern(this, pattern, locale, nullText)

/**
 * Lambda-based formatter for nullable Number.
 */
inline fun Number?.formatWith(
    nullText: String = "-",
    noinline formatter: (Number) -> String
): String = NumberStrings.custom(this, nullText, formatter)

/**
 * Lambda-based formatter that also receives a [Locale].
 */
inline fun Number?.formatWithLocale(
    locale: Locale = Locale.getDefault(),
    nullText: String = "-",
    noinline formatter: (Number, Locale) -> String
): String = NumberStrings.customWithLocale(this, locale, nullText, formatter)

// endregion

// region Bukkit extensions

/**
 * Formats a message describing [value] for this [CommandSender].
 */
fun CommandSender?.formatValue(
    value: Number?,
    label: String = "value",
    locale: Locale = Locale.getDefault()
): String = NumberStrings.forSenderValue(this, value, label, locale)

/**
 * Formats a message describing [value] for this [Player].
 */
fun Player?.formatValue(
    value: Number?,
    label: String = "value",
    locale: Locale = Locale.getDefault()
): String = NumberStrings.forPlayerValue(this, value, label, locale)

/**
 * Formats a message describing [value] for this [OfflinePlayer].
 */
fun OfflinePlayer?.formatValue(
    value: Number?,
    label: String = "value",
    locale: Locale = Locale.getDefault()
): String = NumberStrings.forOfflinePlayerValue(this, value, label, locale)

/**
 * Formats a message describing [value] for this [World].
 */
fun World?.formatValue(
    value: Number?,
    label: String = "value",
    locale: Locale = Locale.getDefault()
): String = NumberStrings.forWorldValue(this, value, label, locale)

/**
 * Formats a message describing [value] for this [Entity].
 */
fun Entity?.formatValue(
    value: Number?,
    label: String = "value",
    locale: Locale = Locale.getDefault()
): String = NumberStrings.forEntityValue(this, value, label, locale)

// endregion