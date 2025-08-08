package sommand.api.v2

/**
 * Represents a single suggestion candidate returned during tab completion.
 * It can hold:
 *  - value: the actual token inserted if the player accepts it
 *  - tooltip: optional short help text (not displayed in legacy Bukkit tab completion list,
 *             but kept for possible Brigadier / Adventure integration in the future)
 *  - permission: optional permission required for this suggestion (if null -> no extra check)
 *
 * Even though Paper 1.20.1 legacy CommandMap tab completion only uses plain strings,
 * keeping this abstraction allows future adapter layers to supply richer suggestion metadata.
 */
interface SommandSuggestion {
    /**
     * Value inserted into the command line when chosen.
     */
    val value: String

    /**
     * Optional tooltip / description (not shown in vanilla list).
     */
    val tooltip: String?

    /**
     * Optional permission requirement (additional fine-grained gating).
     */
    val permission: String?
}

/**
 * Immutable implementation of SommandSuggestion.
 */
data class SimpleSommandSuggestion(
    override val value: String,
    override val tooltip: String? = null,
    override val permission: String? = null
) : SommandSuggestion

/**
 * Factory helpers for building suggestion lists fluently.
 */
object Suggestions {

    /**
     * Wrap a simple collection of strings into SommandSuggestion objects.
     */
    fun of(vararg values: String): List<SommandSuggestion> =
            values.map { SimpleSommandSuggestion(it) }

    /**
     * Map of value -> tooltip into suggestions.
     */
    fun withTooltips(map: Map<String, String>): List<SommandSuggestion> =
            map.map { (v, t) -> SimpleSommandSuggestion(v, t) }

    /**
     * Builder style for programmatic generation with permission or tooltip.
     */
    inline fun build(block: MutableList<SommandSuggestion>.() -> Unit): List<SommandSuggestion> {
        val list = mutableListOf<SommandSuggestion>()
        list.block()
        return list
    }
}

/**
 * Utility to filter suggestions by prefix (case-insensitive) and permission.
 */
fun List<SommandSuggestion>.filterFor(
    prefix: String,
    source: SommandSource
): List<SommandSuggestion> {
    val lower = prefix.lowercase()
    return this
        .filter { s ->
            (s.permission == null || source.sender.hasPermission(s.permission!!)) &&
                    (prefix.isEmpty() || s.value.lowercase().startsWith(lower))
        }
        .sortedBy { it.value.lowercase() }
}

/**
 * Convert suggestions to raw string values (for legacy tab completion).
 */
fun List<SommandSuggestion>.toValues(): List<String> = this.map { it.value }