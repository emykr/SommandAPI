package com.github.emykr.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Strategy interface for formatting numbers.
 * Implementations must be null-safe (return fallback when value is null).
 */
interface NumberFormatter {
    /**
     * Formats the given number (nullable) into a string.
     */
    fun format(value: Number?): String
}

/**
 * Basic formatter using String.valueOf or fallback.
 */
class BasicNumberFormatter(
    private val nullText: String = "-"
) : NumberFormatter {
    override fun format(value: Number?): String =
            NumberStrings.basic(value, nullText)
}

/**
 * Locale-based formatter using pattern "#,##0.###".
 */
class LocaleNumberFormatter(
    private val locale: Locale = Locale.getDefault(),
    private val nullText: String = "-"
) : NumberFormatter {
    override fun format(value: Number?): String =
            NumberStrings.withLocale(value, locale, nullText)
}

/**
 * Pattern-based formatter.
 */
class PatternNumberFormatter(
    private val pattern: String,
    private val locale: Locale = Locale.getDefault(),
    private val nullText: String = "-"
) : NumberFormatter {
    init {
        require(pattern.isNotBlank()) { "Pattern must not be blank." }
    }
    override fun format(value: Number?): String =
            NumberStrings.withPattern(value, pattern, locale, nullText)
}

/**
 * Lambda-based custom formatter (single-arg).
 */
class CustomNumberFormatter(
    private val nullText: String = "-",
    private val formatter: (Number) -> String
) : NumberFormatter {
    init {
        requireNotNull(formatter) { "Formatter must not be null." }
    }
    override fun format(value: Number?): String {
        val number: Number = value ?: return nullText
        return formatter(number)
    }
}

/**
 * Lambda-based custom formatter with locale support.
 */
class LocaleAwareCustomNumberFormatter(
    private val locale: Locale = Locale.getDefault(),
    private val nullText: String = "-",
    private val formatter: (Number, Locale) -> String
) : NumberFormatter {
    init {
        requireNotNull(formatter) { "Formatter must not be null." }
    }
    override fun format(value: Number?): String {
        val number: Number = value ?: return nullText
        return formatter(number, locale)
    }
}

/**
 * Available standard decimal patterns for convenience.
 */
enum class StandardNumberPattern(val pattern: String) {
    WHOLE("#,##0"),
    TWO_DECIMALS("#,##0.00"),
    THREE_DECIMALS("#,##0.000"),
    COMPACT_NO_DECIMALS("#,##0"),
    COMPACT_WITH_DECIMALS("#,##0.###")
}

/**
 * Data class holding raw number and formatted text.
 */
data class FormattedNumber(
    val raw: Number?,
    val text: String
)

/**
 * Simple template definition for replacing a single placeholder with a formatted number.
 */
data class NumericTemplate(
    val template: String,
    val placeholder: String
) {
    init {
        require(template.isNotEmpty()) { "Template must not be empty." }
        require(placeholder.isNotEmpty()) { "Placeholder must not be empty." }
    }

    /**
     * Applies a formatter to a value and returns a FormattedNumber result.
     */
    fun apply(value: Number?, formatter: NumberFormatter): FormattedNumber {
        val formatted = formatter.format(value)
        return FormattedNumber(value, template.replace(placeholder, formatted))
    }
}

/**
 * Strategies for color code conversion handling.
 */
enum class ColorCodeStrategy {
    KEEP,
    STRIP,
    AMPERSAND_TO_SECTION,
    SECTION_TO_AMPERSAND;

    fun apply(input: String?): String {
        return when (this) {
            KEEP -> input ?: ""
            STRIP -> NumberStrings.stripColors(input)
            AMPERSAND_TO_SECTION -> NumberStrings.ampersandToSection(input)
            SECTION_TO_AMPERSAND -> NumberStrings.sectionToAmpersand(input)
        }
    }
}

/**
 * Factory object exposing convenient constructors for NumberFormatter implementations.
 */
object NumberFormatters {

    fun basic(nullText: String = "-"): NumberFormatter =
            BasicNumberFormatter(nullText)

    fun locale(locale: Locale = Locale.getDefault(), nullText: String = "-"): NumberFormatter =
            LocaleNumberFormatter(locale, nullText)

    fun pattern(
        pattern: String,
        locale: Locale = Locale.getDefault(),
        nullText: String = "-"
    ): NumberFormatter = PatternNumberFormatter(pattern, locale, nullText)

    fun standardPattern(
        standard: StandardNumberPattern,
        locale: Locale = Locale.getDefault(),
        nullText: String = "-"
    ): NumberFormatter = PatternNumberFormatter(standard.pattern, locale, nullText)

    fun custom(
        nullText: String = "-",
        formatter: (Number) -> String
    ): NumberFormatter = CustomNumberFormatter(nullText, formatter)

    fun customLocale(
        locale: Locale = Locale.getDefault(),
        nullText: String = "-",
        formatter: (Number, Locale) -> String
    ): NumberFormatter = LocaleAwareCustomNumberFormatter(locale, nullText, formatter)
}