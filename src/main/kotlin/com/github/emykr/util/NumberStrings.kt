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
 * 기능:
 * - Number → String 기본 변환
 * - Locale 기반 포맷, 패턴 포맷
 * - 접두/접미어, 템플릿 치환
 * - Bukkit 관련 메시지 포맷 (Sender/Player/World/Entity 등)
 * - 람다식 기반 커스텀 포맷터 지원
 * - 확장 함수 기반 헬퍼 제공
 */
object NumberStrings {

    // region: Basic conversion

    /**
     * Converts a nullable [Number] to a string.
     *
     * - When [value] is null, [nullText] is returned (default: "-").
     */
    @JvmStatic
    @JvmOverloads
    fun basic(
        value: Number?,
        nullText: String = "-"
    ): String {
        val number: Number = value ?: return nullText
        return java.lang.String.valueOf(number)
    }

    /**
     * Converts a nullable [Number] to a string with locale-aware formatting.
     *
     * Example:
     * - locale=Locale.US, value=12345.67 → "12,345.67"
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
     * Converts a nullable [Number] using a custom decimal pattern.
     *
     * @param pattern DecimalFormat pattern, e.g. "#,##0", "0.00", "#,##0.000".
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

    // region: Lambda-based custom formatting

    /**
     * Applies a custom formatter lambda to a nullable number.
     *
     * - [formatter] 는 null 이 아닌 Number 에만 호출됩니다.
     * - [value] 가 null 이면 [nullText] 를 반환합니다.
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
     * Locale 정보를 함께 받는 커스텀 포맷터.
     *
     * 예:
     *  customWithLocale(value, Locale.KOREA) { n, loc -> "${withLocale(n, loc)}원" }
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

    // endregion

    // region: Prefix / suffix / template helpers

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
     * template = "You have {amount} coins."
     * placeholder = "{amount}"
     * value = 10  → "You have 10 coins."
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

    // endregion

    // region: Bukkit-specific message helpers

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

/**
 * ===== 확장 함수 영역 =====
 *
 * Number? / CommandSender / Player / World / Entity 등에
 * 자연스럽게 붙여 쓸 수 있는 확장 함수들입니다.
 */

/**
 * Nullable Number 에 대한 기본 문자열 변환 확장.
 */
fun Number?.toBasicString(nullText: String = "-"): String =
        NumberStrings.basic(this, nullText)

/**
 * Nullable Number 에 대한 Locale 기반 문자열 변환 확장.
 */
fun Number?.toLocalizedString(
    locale: Locale = Locale.getDefault(),
    nullText: String = "-"
): String = NumberStrings.withLocale(this, locale, nullText)

/**
 * Nullable Number 에 대한 패턴 기반 문자열 변환 확장.
 */
fun Number?.toPatternString(
    pattern: String,
    locale: Locale = Locale.getDefault(),
    nullText: String = "-"
): String = NumberStrings.withPattern(this, pattern, locale, nullText)

/**
 * 람다식으로 Number → String 변환을 지정하는 확장.
 */
inline fun Number?.formatWith(
    nullText: String = "-",
    noinline formatter: (Number) -> String
): String = NumberStrings.custom(this, nullText, formatter)

/**
 * Locale 을 함께 고려하는 람다 포맷 확장.
 */
inline fun Number?.formatWithLocale(
    locale: Locale = Locale.getDefault(),
    nullText: String = "-",
    noinline formatter: (Number, Locale) -> String
): String = NumberStrings.customWithLocale(this, locale, nullText, formatter)

/**
 * CommandSender 기준 메시지 생성 확장.
 */
fun CommandSender?.formatValue(
    value: Number?,
    label: String = "value",
    locale: Locale = Locale.getDefault()
): String = NumberStrings.forSenderValue(this, value, label, locale)

/**
 * Player 기준 메시지 생성 확장.
 */
fun Player?.formatValue(
    value: Number?,
    label: String = "value",
    locale: Locale = Locale.getDefault()
): String = NumberStrings.forPlayerValue(this, value, label, locale)

/**
 * OfflinePlayer 기준 메시지 생성 확장.
 */
fun OfflinePlayer?.formatValue(
    value: Number?,
    label: String = "value",
    locale: Locale = Locale.getDefault()
): String = NumberStrings.forOfflinePlayerValue(this, value, label, locale)

/**
 * World 기준 메시지 생성 확장.
 */
fun World?.formatValue(
    value: Number?,
    label: String = "value",
    locale: Locale = Locale.getDefault()
): String = NumberStrings.forWorldValue(this, value, label, locale)

/**
 * Entity 기준 메시지 생성 확장.
 */
fun Entity?.formatValue(
    value: Number?,
    label: String = "value",
    locale: Locale = Locale.getDefault()
): String = NumberStrings.forEntityValue(this, value, label, locale)