package io.github.emykr.sommand.api.v2.dsl

import sommand.api.v2.CommandArgument

/**
 * Convenience re-export factories inside DSL scope.
 */
object arg {
    fun string(name: String, allowEmpty: Boolean = false) = CommandArgument.string(name, allowEmpty)
    fun greedyString(name: String) = CommandArgument.greedyString(name)
    fun int(name: String, min: Int? = null, max: Int? = null) = CommandArgument.int(name, min, max)
    fun double(name: String, min: Double? = null, max: Double? = null) = CommandArgument.double(name, min, max)
    fun boolean(name: String) = CommandArgument.boolean(name)
    fun player(name: String) = CommandArgument.player(name)
    inline fun <reified E : Enum<E>> enum(name: String) = CommandArgument.enum<E>(name)
}