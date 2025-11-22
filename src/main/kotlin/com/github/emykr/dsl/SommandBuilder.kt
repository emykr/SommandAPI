package com.github.emykr.dsl

import org.bukkit.plugin.java.JavaPlugin
import com.github.emykr.CommandArgument
import com.github.emykr.CommandRegistry
import com.github.emykr.compat.BaseCompat
import com.github.emykr.node.ArgumentNode
import com.github.emykr.node.LiteralNode
import com.github.emykr.node.RootNode
import com.github.emykr.node.SommandNode

/**
 * DSL entry builder for defining one or multiple commands.
 *
 * This class focuses only on building command trees.
 * A BaseCompat instance can be injected for version-specific behavior or messages.
 *
 * 기존 사용 코드는 그대로 유지:
 *   SommandBuilder(plugin).command(...)  (compat 필요 없을 때)
 * 새로운 기능:
 *   SommandBuilder(plugin, compat).command(...)  (버전별 커스터마이즈 필요 시)
 */
class SommandBuilder internal constructor(
    private val plugin: JavaPlugin,
    private val compat: BaseCompat? = null
) {

    /**
     * Exposes the compat instance if provided.
     * Returns null when no compat was injected.
     */
    fun currentCompat(): BaseCompat? = compat

    /**
     * Declares and registers a root command.
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
        val treeBuilder = CommandTreeBuilder(root)
        treeBuilder.block()
        CommandRegistry.add(root)
    }

    /**
     * Convenience for a short command with only a direct executor.
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
 * Execution block type bound to SommandNode.ExecutionScope.
 */
typealias CommandExecution = SommandNode.ExecutionScope.() -> Unit

/**
 * Internal tree builder for a single command root.
 */
class CommandTreeBuilder internal constructor(
    private val current: SommandNode
) {

    /**
     * Adds a literal child node.
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
     * Adds a literal node with an executor.
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
     * Adds an argument node with direct executor.
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
     * Marks current node executable.
     */
    fun executes(block: CommandExecution) {
        current.executor = block
    }

    /**
     * Group sugar (internally just a literal).
     */
    fun group(name: String, block: CommandTreeBuilder.() -> Unit) {
        literal(name, block = block)
    }
}