package sommand.api.v2.node

import sommand.api.v2.CommandArgument

/**
 * Base tree node for command routing.
 */
sealed class SommandNode(
    val name: String,
    val description: String?,
    val permission: String?
) {
    internal val children: MutableList<SommandNode> = mutableListOf()
    internal var executor: (ExecutionScope.() -> Unit)? = null
    internal var greedy: Boolean = false // marker when this node should swallow remaining input (for greedy string)
    internal var argument: CommandArgument<*>? = null

    fun hasExecutor(): Boolean = executor != null

    /**
     * Execution scope passed to executor lambdas.
     */
    class ExecutionScope(
        val parsedArguments: MutableMap<String, Any>,
        val trigger: () -> Unit
    )
}

/**
 * Represents a literal (static token).
 */
class LiteralNode(
    name: String,
    description: String?,
    permission: String?
) : SommandNode(name, description, permission)

/**
 * Represents an argument node (dynamic token).
 */
class ArgumentNode(
    val arg: CommandArgument<*>,
    description: String?,
    permission: String?,
    greedy: Boolean = false
) : SommandNode(arg.name, description, permission) {
    init {
        argument = arg
        this.greedy = greedy
    }
}

/**
 * Root node per registered command entry point (first alias).
 */
class RootNode(
    name: String,
    description: String?,
    permission: String?,
    val aliases: List<String>
) : SommandNode(name, description, permission)