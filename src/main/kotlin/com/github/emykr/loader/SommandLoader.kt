package com.github.emykr.dsl

import org.bukkit.plugin.java.JavaPlugin
import com.github.emykr.CommandArgument
import com.github.emykr.CommandRegistry
import com.github.emykr.SommandImpl
import com.github.emykr.node.ArgumentNode
import com.github.emykr.node.LiteralNode
import com.github.emykr.node.RootNode
import com.github.emykr.node.SommandNode

/**
 * DSL entry builder for defining one or multiple commands.
 *
 * Updated names for readability:
 * - subcommand(...) replaces literal(...)
 * - param(...) replaces argument(...)
 *
 * Backward compatibility:
 * - literal(...), literalExec(...), argument(...), argumentExec(...) remain as deprecated
 *   and delegate to the new API so existing code keeps working.
 */
class SommandBuilder internal constructor(
    private val plugin: JavaPlugin
) {

    /**
     * Declares and registers a root command immediately (legacy-compatible behavior).
     */
    fun command(
        vararg aliases: String,
        description: String? = null,
        permission: String? = null,
        block: CommandTreeBuilder.() -> Unit
    ) {
        require(aliases.isNotEmpty()) { "At least one alias must be provided." }
        val main = aliases.first()
        val root = RootNode(main, description, permission, aliases.toList())
        // Build the tree
        CommandTreeBuilder(root).block()
        // Register
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

/**
 * Type alias for command execution lambdas bound to SommandNode.ExecutionScope.
 */
typealias CommandExecution = SommandNode.ExecutionScope.() -> Unit

/**
 * Builds the internal tree for a single command root.
 *
 * Primary APIs:
 * - subcommand(name, ..., executes?, block?)  // replaces literal(...)
 * - param(arg, ..., greedy=false, executes?, block?)  // replaces argument(...)
 * - executes { ... }  // mark current node as executable
 *
 * Deprecated aliases remain and delegate to the new ones.
 */
class CommandTreeBuilder internal constructor(
    private val current: SommandNode
) {

    /**
     * Adds a subcommand (literal) child node.
     *
     * Examples:
     * subcommand("join", executes = { ... })
     * subcommand("cheat") { ...nested... }
     * subcommand("give", executes = { ... }) { param(StringArg("target")) { ... } }
     */
    fun subcommand(
        name: String,
        description: String? = null,
        permission: String? = null,
        executes: CommandExecution? = null,
        block: CommandTreeBuilder.() -> Unit = {}
    ) {
        val lit = LiteralNode(name, description, permission)
        // Attach executor if provided
        if (executes != null) {
            lit.executor = executes
        }
        // Link as child
        current.children += lit
        // Build nested children
        CommandTreeBuilder(lit).block()
    }

    /**
     * Adds an argument node as a parameter under the current node.
     *
     * Examples:
     * param(StringArg("target"), executes = { ... })
     * param(IntArg("amount"), greedy = false) { subcommand("confirm", executes = { ... }) }
     */
    fun <T : Any> param(
        arg: CommandArgument<T>,
        description: String? = null,
        permission: String? = null,
        greedy: Boolean = false,
        executes: CommandExecution? = null,
        block: CommandTreeBuilder.() -> Unit = {}
    ) {
        val an = ArgumentNode(arg, description, permission, greedy)
        // Attach executor if provided
        if (executes != null) {
            an.executor = executes
        }
        // Link as child
        current.children += an
        // Build nested children
        CommandTreeBuilder(an).block()
    }

    /**
     * Mark current node as executable.
     */
    fun executes(block: CommandExecution) {
        current.executor = block
    }

    /**
     * DSL sugar for an optional sub-branch with a group name.
     * Internally the same as subcommand(name) { block() }.
     */
    fun group(name: String, block: CommandTreeBuilder.() -> Unit) {
        subcommand(name, block = block)
    }

    // ----------------------------
    // Backward-compatible aliases
    // ----------------------------

    /**
     * Deprecated: use subcommand(name, ..., executes?, block?)
     */
    @Deprecated(
        message = "Use subcommand(name, description, permission, executes, block) instead.",
        replaceWith = ReplaceWith("subcommand(name, description, permission, executes = null, block)")
    )
    fun literal(
        name: String,
        description: String? = null,
        permission: String? = null,
        block: CommandTreeBuilder.() -> Unit
    ) {
        subcommand(name, description, permission, executes = null, block = block)
    }

    /**
     * Deprecated: use subcommand(name, ..., executes = { ... })
     */
    @Deprecated(
        message = "Use subcommand(name, description, permission, executes = executes) instead.",
        replaceWith = ReplaceWith("subcommand(name, description, permission, executes = executes)")
    )
    fun literalExec(
        name: String,
        description: String? = null,
        permission: String? = null,
        executes: CommandExecution
    ) {
        subcommand(name, description, permission, executes = executes)
    }

    /**
     * Deprecated: use param(arg, ..., executes?, block?)
     */
    @Deprecated(
        message = "Use param(arg, description, permission, greedy, executes, block) instead.",
        replaceWith = ReplaceWith("param(arg, description, permission, greedy, executes = null, block)")
    )
    fun <T : Any> argument(
        arg: CommandArgument<T>,
        description: String? = null,
        permission: String? = null,
        greedy: Boolean = false,
        block: CommandTreeBuilder.() -> Unit
    ) {
        param(arg, description, permission, greedy, executes = null, block = block)
    }

    /**
     * Deprecated: use param(arg, ..., executes = { ... })
     */
    @Deprecated(
        message = "Use param(arg, description, permission, greedy, executes = executes) instead.",
        replaceWith = ReplaceWith("param(arg, description, permission, greedy, executes = executes)")
    )
    fun <T : Any> argumentExec(
        arg: CommandArgument<T>,
        description: String? = null,
        permission: String? = null,
        greedy: Boolean = false,
        executes: CommandExecution
    ) {
        param(arg, description, permission, greedy, executes = executes)
    }
}

/**
 * Top-level DSL entry point.
 *
 * Single, non-duplicated definition to avoid "Conflicting overloads".
 */
fun sommand(plugin: JavaPlugin, block: SommandBuilder.() -> Unit) {
    // Load and register the DSL-defined commands into the registry via SommandImpl
    SommandImpl.load(plugin, block)
}