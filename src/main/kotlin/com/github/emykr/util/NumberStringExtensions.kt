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
 * These are thin, readable wrappers over [NumberStrings].
 *
 * 주의:
 * - 이 파일은 한 번만 정의되어야 합니다.
 * - 예전에 작성된 동일 시그니처의 확장 함수가 다른 파일에 남아 있으면
 *   "Conflicting overloads" 오류가 발생합니다.
 *
 * 따라서 Number 관련/ Bukkit 관련 확장 함수는 이 파일에서만 정의하는 것을 권장합니다.
 */

// region Number extensions

/**
 * Shortcut for [NumberStrings.basic].
 *
 * Nullable Number 를 간단히 문자열로 바꿉니다.
 */
fun Number?.toBasicString(nullText: String = "-"): String =
        NumberStrings.basic(this, nullText)

/**
 * Shortcut for [NumberStrings.withLocale].
 *
 * 로케일 기반 포맷 (예: 12,345.67) 을 사용합니다.
 */
fun Number?.toLocalizedString(
    locale: Locale = Locale.getDefault(),
    nullText: String = "-"
): String = NumberStrings.withLocale(this, locale, nullText)

/**
 * Shortcut for [NumberStrings.withPattern].
 *
 * 패턴 기반 포맷 (예: "#,##0", "0.00") 을 사용합니다.
 */
fun Number?.toPatternString(
    pattern: String,
    locale: Locale = Locale.getDefault(),
    nullText: String = "-"
): String = NumberStrings.withPattern(this, pattern, locale, nullText)

/**
 * Lambda-based formatter for nullable Number.
 *
 * 예:
 *  value.formatWith { n -> "%.2f".format(n.toDouble()) }
 */
inline fun Number?.formatWith(
    nullText: String = "-",
    noinline formatter: (Number) -> String
): String = NumberStrings.custom(this, nullText, formatter)

/**
 * Lambda-based formatter that also receives a [Locale].
 *
 * 예:
 *  value.formatWithLocale(Locale.KOREA) { n, loc ->
 *      NumberStrings.withLocale(n, loc) + "원"
 *  }
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
 *
 * 예: "Sender Steve has lives: 5"
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