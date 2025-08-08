package sommand.api.v2

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.Locale
import kotlin.reflect.KClass

/**
 * 하나의 명령 인자 정의.
 *
 * @param name       인자 논리 이름 (파싱 저장 키)
 * @param type       리플렉션/디버깅용 타입 토큰
 * @param parser     문자열 -> T? 파서
 * @param suggester  (prefix, source) -> List<SommandSuggestion>
 * @param validator  파싱 후 값 허용 여부 필터
 */
class CommandArgument<T : Any>(
    val name: String,
    private val type: KClass<T>,
    private val parser: (String) -> T?,
    private val suggester: (prefix: String, source: SommandSource) -> List<SommandSuggestion> = { _, _ -> emptyList() },
    private val validator: (T) -> Boolean = { true }
) {

    /**
     * 토큰을 T 로 파싱 (실패 시 null).
     */
    fun parse(token: String): T? {
        val parsed = parser(token) ?: return null
        return if (validator(parsed)) parsed else null
    }

    /**
     * 탭 완성 후보.
     */
    fun suggest(prefix: String, source: SommandSource): List<SommandSuggestion> =
            suggester(prefix, source).filterFor(prefix, source)

    override fun toString(): String = "<$name:${type.simpleName}>"

    companion object {

        fun string(name: String, allowEmpty: Boolean = false): CommandArgument<String> =
                CommandArgument(
                    name = name,
                    type = String::class,
                    parser = { if (!allowEmpty && it.isEmpty()) null else it },
                    suggester = { _, _ -> emptyList() }
                )

        fun greedyString(name: String): CommandArgument<String> =
                CommandArgument(
                    name = name,
                    type = String::class,
                    parser = { it },
                    suggester = { _, _ -> emptyList() }
                )

        fun int(name: String, min: Int? = null, max: Int? = null): CommandArgument<Int> =
                CommandArgument(
                    name = name,
                    type = Int::class,
                    parser = { it.toIntOrNull() },
                    validator = { v -> (min == null || v >= min) && (max == null || v <= max) }
                )

        fun double(name: String, min: Double? = null, max: Double? = null): CommandArgument<Double> =
                CommandArgument(
                    name = name,
                    type = Double::class,
                    parser = { it.toDoubleOrNull() },
                    validator = { v -> (min == null || v >= min) && (max == null || v <= max) }
                )

        fun boolean(name: String): CommandArgument<Boolean> =
                CommandArgument(
                    name = name,
                    type = Boolean::class,
                    parser = {
                        when (it.toLowerCase(Locale.ROOT)) {
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
                    name = name,
                    type = Player::class,
                    parser = { token -> Bukkit.getPlayerExact(token) },
                    suggester = { prefix, _ ->
                        val lower = prefix.toLowerCase(Locale.ROOT)
                        Bukkit.getOnlinePlayers()
                            .asSequence()
                            .map { it.name }
                            .filter { lower.isEmpty() || it.toLowerCase(Locale.ROOT).startsWith(lower) }
                            .map { SimpleSommandSuggestion(it) }
                            .toList()
                    }
                )

        inline fun <reified E : Enum<E>> enum(name: String): CommandArgument<E> {
            val values: List<E> = enumValues<E>().toList()
            return CommandArgument(
                name = name,
                type = E::class,
                parser = { token ->
                    values.firstOrNull { it.name.equals(token, ignoreCase = true) }
                },
                suggester = { prefix, _ ->
                    val lower = prefix.toLowerCase(Locale.ROOT)
                    values
                        .map { it.name.toLowerCase(Locale.ROOT) }
                        .filter { lower.isEmpty() || it.startsWith(lower) }
                        .map { SimpleSommandSuggestion(it) }
                }
            )
        }
    }
}