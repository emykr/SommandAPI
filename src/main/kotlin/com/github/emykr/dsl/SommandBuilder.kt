package com.github.emykr.dsl

import org.bukkit.plugin.java.JavaPlugin
import com.github.emykr.CommandArgument
import com.github.emykr.CommandRegistry
import com.github.emykr.SommandImpl
import com.github.emykr.node.*

/**
 * DSL entry builder for defining one or multiple commands.
 */
class SommandBuilder internal constructor(
    private val plugin: JavaPlugin
) {

    fun command(
        vararg aliases: String,
        description: String? = null,
        permission: String? = null,
        block: CommandTreeBuilder.() -> Unit
    ) {
        require(aliases.isNotEmpty()) { "At least one alias must be provided." }
        val main = aliases.first()
        val root = RootNode(main, description, permission, aliases.toList())
        val treeBuilder = CommandTreeBuilder(root)
        treeBuilder.block()
        CommandRegistry.add(root)
    }

    /**
     * Inline convenience for a very short command with a direct executor.
     */
    fun simple(
        vararg aliases: String,
        description: String? = null,
        permission: String? = null,
        executes: CommandExecution = {}
    ) {
        command(*aliases, description = description, permission = permission) {
            executes(executes)
        }
    }
}

typealias CommandExecution = SommandNode.ExecutionScope.() -> Unit

/**
 * Builds the internal tree for a single command root.
 */
class CommandTreeBuilder internal constructor(
    private val current: SommandNode
) {

    /**
     * Adds a literal child.
     */
    fun literal(
        name: String,
        description: String? = null,
        permission: String? = null,
        block: CommandTreeBuilder.() -> Unit
    ) {
        val lit = LiteralNode(name, description, permission)
        current.children += lit
        CommandTreeBuilder(lit).block()
    }

    /**
     * Adds a literal with an executor directly.
     */
    fun literalExec(
        name: String,
        description: String? = null,
        permission: String? = null,
        executes: CommandExecution
    ) {
        val lit = LiteralNode(name, description, permission)
        lit.executor = executes
        current.children += lit
    }

    /**
     * Adds an argument node.
     */
    fun <T : Any> argument(
        arg: CommandArgument<T>,
        description: String? = null,
        permission: String? = null,
        greedy: Boolean = false,
        block: CommandTreeBuilder.() -> Unit
    ) {
        val an = ArgumentNode(arg, description, permission, greedy)
        current.children += an
        CommandTreeBuilder(an).block()
    }

    /**
     * Adds argument with direct executor.
     */
    fun <T : Any> argumentExec(
        arg: CommandArgument<T>,
        description: String? = null,
        permission: String? = null,
        greedy: Boolean = false,
        executes: CommandExecution
    ) {
        val an = ArgumentNode(arg, description, permission, greedy)
        an.executor = executes
        current.children += an
    }

    /**
     * Mark current node as executable.
     */
    fun executes(block: CommandExecution) {
        current.executor = block
    }

    /**
     * DSL sugar for optional sub-branch with a group name (non-literal).
     */
    fun group(name: String, block: CommandTreeBuilder.() -> Unit) {
        // group is effectively a literal
        literal(name, block = block)
    }
}

/**
 * Top-level DSL function.
 */
fun sommand(plugin: JavaPlugin, block: SommandBuilder.() -> Unit) {
    SommandImpl.load(plugin, block)
}