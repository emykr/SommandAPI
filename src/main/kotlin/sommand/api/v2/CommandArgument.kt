package sommand.api.v2

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*
import kotlin.reflect.KClass

/**
 * Represents a single command argument definition with:
 * - name
 * - parser (String -> T?)
 * - tab completion provider
 * - optional validation predicate
 */
class CommandArgument<T : Any>(
    val name: String,
    private val type: KClass<T>,
    private val parser: (String) -> T?,
    private val suggester: (prefix: String) -> List<String> = { emptyList() },
    private val validator: (T) -> Boolean = { true }
) {

    /**
     * Tries to parse a raw token into the target type.
     * Returns null when parsing fails or validation fails.
     */
    fun parse(token: String): T? {
        val parsed = parser(token) ?: return null
        return if (validator(parsed)) parsed else null
    }

    /**
     * Provides suggestions for tab completion given current token prefix.
     */
    fun suggest(prefix: String): List<String> = suggester(prefix)
        .filter { it.startsWith(prefix, ignoreCase = true) }
        .sorted()

    override fun toString(): String = "<$name:${type.simpleName}>"

    companion object {
        fun string(name: String, allowEmpty: Boolean = false): CommandArgument<String> =
                CommandArgument(
                    name,
                    String::class,
                    parser = {
                        if (!allowEmpty && it.isEmpty()) null else it
                    },
                    suggester = { emptyList() }
                )

        fun greedyString(name: String): CommandArgument<String> =
                CommandArgument(
                    name,
                    String::class,
                    parser = { it },
                    suggester = { emptyList() }
                )

        fun int(name: String, min: Int? = null, max: Int? = null): CommandArgument<Int> =
                CommandArgument(
                    name,
                    Int::class,
                    parser = { it.toIntOrNull() },
                    suggester = { emptyList() },
                    validator = { value ->
                        (min == null || value >= min) && (max == null || value <= max)
                    }
                )

        fun double(name: String, min: Double? = null, max: Double? = null): CommandArgument<Double> =
                CommandArgument(
                    name,
                    Double::class,
                    parser = { it.toDoubleOrNull() },
                    suggester = { emptyList() },
                    validator = { value ->
                        (min == null || value >= min) && (max == null || value <= max)
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
                    suggester = { listOf("true", "false") }
                )

        fun player(name: String): CommandArgument<Player> =
                CommandArgument(
                    name,
                    Player::class,
                    parser = { token -> Bukkit.getPlayerExact(token) },
                    suggester = { prefix ->
                        Bukkit.getOnlinePlayers()
                            .map { it.name }
                            .filter { it.startsWith(prefix, ignoreCase = true) }
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
                suggester = { prefix ->
                    values.map { it.name.lowercase(Locale.ENGLISH) }
                        .filter { it.startsWith(prefix.lowercase(Locale.ENGLISH)) }
                }
            )
        }
    }
}