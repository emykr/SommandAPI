package com.github.emykr.fabric.dsl

import com.github.emykr.CommandArgument
import com.github.emykr.CommandRegistry
import com.github.emykr.node.*

/**
 * Fabric-side DSL builder. Mirrors the Paper/Bukkit SommandBuilder but without a JavaPlugin.
 * It only constructs the command tree and registers roots in the shared CommandRegistry.
 */
class SommandBuilder {

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

class CommandTreeBuilder(private val current: SommandNode) {

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

    fun executes(block: CommandExecution) {
        current.executor = block
    }

    fun group(name: String, block: CommandTreeBuilder.() -> Unit) {
        literal(name, block = block)
    }
}

fun sommandFabric(block: SommandBuilder.() -> Unit) {
    val builder = SommandBuilder()
    builder.block()
}

