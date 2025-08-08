package sommand.api.v2

/**
 * Concrete runtime context passed into command execution lambdas.
 * Contains:
 * - source (the command sender abstraction)
 * - raw input tokens
 * - parsed argument values map
 */
class CommandContext(
    val source: SommandSource,
    val label: String,
    val rawArgs: List<String>,
    private val parsed: Map<String, Any>
) {

    /**
     * Generic typed getter. Throws IllegalArgumentException if absent or wrong type.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> get(name: String, type: Class<T>): T {
        val value = parsed[name]
            ?: throw IllegalArgumentException("Argument '$name' was not provided.")
        if (!type.isInstance(value)) {
            throw IllegalArgumentException("Argument '$name' expected ${type.simpleName} but got ${value::class.java.simpleName}.")
        }
        return value as T
    }

    inline fun <reified T : Any> get(name: String): T = get(name, T::class.java)

    /**
     * Returns an immutable snapshot of parsed arguments (for debugging / logging).
     */
    fun all(): Map<String, Any> = parsed.toMap()
}