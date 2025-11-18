package com.github.emykr.dsl

import com.github.emykr.CommandArgument
import org.bukkit.entity.Player

/**
 * Convenience factory wrapper for [CommandArgument] builders inside the DSL scope.
 *
 * This object:
 * - Re-exports the most common argument factory methods used in the DSL.
 * - Performs basic validation on argument names and range parameters to fail fast
 *   on programmer errors (e.g., blank names, invalid min/max).
 *
 * Usage example:
 * ```kotlin
 * command("example") {
 *     argumentExec(Args.string("name")) {
 *         val name = arg<String>("name")
 *         sender.sendMessage("Hello, $name")
 *     }
 * }
 * ```
 */
object Args {

    /**
     * Creates a string argument with an optional empty-value allowance.
     *
     * @param name logical argument name; must not be blank.
     * @param allowEmpty if true, empty strings are accepted as valid values.
     */
    fun string(
        name: String,
        allowEmpty: Boolean = false
    ): CommandArgument<String> {
        require(name.isNotBlank()) { "Argument name for string() must not be blank." }
        return CommandArgument.string(name, allowEmpty)
    }

    /**
     * Creates a greedy string argument that consumes all remaining tokens.
     *
     * @param name logical argument name; must not be blank.
     */
    fun greedyString(name: String): CommandArgument<String> {
        require(name.isNotBlank()) { "Argument name for greedyString() must not be blank." }
        return CommandArgument.greedyString(name)
    }

    /**
     * Creates an integer argument with optional min/max bounds.
     *
     * @param name logical argument name; must not be blank.
     * @param min optional lower bound (inclusive).
     * @param max optional upper bound (inclusive).
     *
     * @throws IllegalArgumentException if min > max.
     */
    fun int(
        name: String,
        min: Int? = null,
        max: Int? = null
    ): CommandArgument<Int> {
        require(name.isNotBlank()) { "Argument name for int() must not be blank." }
        if (min != null && max != null) {
            require(min <= max) {
                "Invalid range for int($name): min ($min) must be <= max ($max)."
            }
        }
        return CommandArgument.int(name, min, max)
    }

    /**
     * Creates a double argument with optional min/max bounds.
     *
     * @param name logical argument name; must not be blank.
     * @param min optional lower bound (inclusive).
     * @param max optional upper bound (inclusive).
     *
     * @throws IllegalArgumentException if min > max.
     */
    fun double(
        name: String,
        min: Double? = null,
        max: Double? = null
    ): CommandArgument<Double> {
        require(name.isNotBlank()) { "Argument name for double() must not be blank." }
        if (min != null && max != null) {
            require(min <= max) {
                "Invalid range for double($name): min ($min) must be <= max ($max)."
            }
        }
        return CommandArgument.double(name, min, max)
    }

    /**
     * Creates a boolean argument.
     *
     * @param name logical argument name; must not be blank.
     */
    fun boolean(name: String): CommandArgument<Boolean> {
        require(name.isNotBlank()) { "Argument name for boolean() must not be blank." }
        return CommandArgument.boolean(name)
    }

    /**
     * Creates a player argument.
     *
     * NOTE:
     * - The exact generic type depends on [CommandArgument.player] definition.
     * - If that factory returns `CommandArgument<Player>`, we expose the same here
     *   to avoid unchecked casts and type mismatches.
     *
     * @param name logical argument name; must not be blank.
     */
    fun player(name: String): CommandArgument<Player> {
        require(name.isNotBlank()) { "Argument name for player() must not be blank." }
        return CommandArgument.player(name)
    }

    /**
     * Creates an enum argument for the given enum type.
     *
     * @param name logical argument name; must not be blank.
     */
    inline fun <reified E : Enum<E>> enum(name: String): CommandArgument<E> {
        require(name.isNotBlank()) { "Argument name for enum() must not be blank." }
        return CommandArgument.enum(name)
    }
}