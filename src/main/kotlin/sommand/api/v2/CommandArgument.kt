package sommand.api.v2

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*
import kotlin.reflect.KClass

/**
 * Represents a typed argument for a command.
 * Parsing + suggestion providers are unified around SommandSuggestion.
 *
 * @param name Logical name (key) used in parsing map.
 * @param type Kotlin class token to help with diagnostics.
 * @param parser String -> T? parse function.
 * @param suggester (prefix, source) -> List<SommandSuggestion> dynamic provider.
 * @param validator Additional acceptance filter for parsed values.
 */
class CommandArgument<T : Any>(
    val name: String,
    private val type: KClass<T>,
    private val parser: (String) -> T?,
    private val suggester: (prefix: String, source: SommandSource) -> List<SommandSuggestion> = { _, _ -> emptyList() },
    private val validator: (T) -> Boolean = { true }
) {

    /**
     * Attempts to parse the raw token into target type.
     * Returns null if parsing fails or validator rejects.
     */
    fun parse(token: String): T? {
        val parsed = parser(token) ?: return null
        return if (validator(parsed)) parsed else null
    }

    /**
     * Provides suggestions given a prefix & command source for contextual filtering.
     */
    fun suggest(prefix: String, source: SommandSource): List<SommandSuggestion> =
            suggester(prefix, source).filterFor(prefix, source)

    override fun toString(): String = "<$name:${type.simpleName}>"

    companion object {

        private fun wrap(values: List<String>): (String, SommandSource) -> List<SommandSuggestion> =
                { prefix, source ->
                    values.map { SimpleSommandSuggestion(it) }.filterFor(prefix, source)
                }

        fun string(name: String, allowEmpty: Boolean = false): CommandArgument<String> =
                CommandArgument(
                    name,
                    String::class,
                    parser = { if (!allowEmpty && it.isEmpty()) null else it },
                    suggester = { _, _ -> emptyList() }
                )

        fun greedyString(name: String): CommandArgument<String> =
                CommandArgument(
                    name,
                    String::class,
                    parser = { it },
                    suggester = { _, _ -> emptyList() }
                )

        fun int(name: String, min: Int? = null, max: Int? = null): CommandArgument<Int> =
                CommandArgument(
                    name,
                    Int::class,
                    parser = { it.toIntOrNull() },
                    validator = { v ->
                        (min == null || v >= min) && (max == null || v <= max)
                    }
                )

        fun double(name: String, min: Double? = null, max: Double? = null): CommandArgument<Double> =
                CommandArgument(
                    name,
                    Double::class,
                    parser = { it.toDoubleOrNull() },
                    validator = { v ->
                        (min == null || v >= min) && (max == null || v <= max)
                    }
                )

        fun boolean(name: String): CommandArgument<Boolean> =
                CommandArgument(
                    name,
                    Boolean::class,
                    parser = {
                        when (it.lowercase(Locale.ENGLISH)) {
                            "true", "yes", "y", "on", "1" -> true
                            "false", "no", "n", "off", "0" -> false
                            else -> null
                        }
                    },
                    suggester = { _, _ ->
                        listOf(
                            SimpleSommandSuggestion("true", "Boolean true"),
                            SimpleSommandSuggestion("false", "Boolean false")
                        )
                    }
                )

        fun player(name: String): CommandArgument<Player> =
                CommandArgument(
                    name,
                    Player::class,
                    parser = { token -> Bukkit.getPlayerExact(token) },
                    suggester = { _, _ ->
                        Bukkit.getOnlinePlayers().map { SimpleSommandSuggestion(it.name) }
                    }
                )

        inline fun <reified E : Enum<E>> enum(name: String): CommandArgument<E> {
            val values = enumValues<E>().toList()
            return CommandArgument(
                name,
                E::class,
                parser = { token ->
                    values.firstOrNull { it.name.equals(token, ignoreCase = true) }
                },
                suggester = { _, _ ->
                    values.map { SimpleSommandSuggestion(it.name.lowercase(Locale.ENGLISH)) }
                }
            )
        }
    }
}