package com.github.emykr.dispatcher

import com.github.emykr.compat.BaseCompat
import com.github.emykr.CommandRegistry
import com.github.emykr.node.*
import org.bukkit.command.CommandSender

/**
 * Dispatcher that leverages BaseCompat for all user-facing messages.
 *
 * Assumptions (interface contract – inferred):
 * - dispatch(sender, label, argsTokens): returns true if command handled.
 * - suggest(sender, label, argsTokens): returns tab suggestions.
 *
 * If an existing SommandDispatcher interface differs, adjust method signatures accordingly.
 */
class CompatAwareDispatcher(
    private val compat: BaseCompat
) : SommandDispatcher {

    /**
     * Attempts to dispatch the command based on the tokens.
     */
    override fun dispatch(sender: CommandSender, label: String, tokens: List<String>): Boolean {
        val root = CommandRegistry.find(label) ?: return false

        // Permission check on root
        if (!checkPermission(sender, root.permission)) {
            sender.sendMessage(compat.noPermissionMessage())
            return true
        }

        // Traverse tree
        val context = TraversalContext(sender, label, tokens)
        val node = traverse(root, context)

        if (node == null) {
            // Unknown subcommand or path
            sender.sendMessage(compat.unknownSubcommandMessage(root.aliases.first()))
            return true
        }

        // If executable, run
        val exec = node.executor
        if (exec != null) {
            try {
                val scope = SommandNode.ExecutionScope(sender, label, tokens, context.argumentsCollected)
                exec.invoke(scope)
            } catch (ex: Exception) {
                sender.sendMessage(compat.executionErrorMessage())
                // (선택) 로거에 스택 트레이스 출력
                ex.printStackTrace()
            }
            return true
        } else {
            // No executor on exact node
            sender.sendMessage(compat.unknownSubcommandMessage(root.aliases.first()))
            return true
        }
    }

    /**
     * Provides suggestions based on partial tokens.
     * Simplified: only lists immediate child literal names or argument hints.
     */
    override fun suggest(sender: CommandSender, label: String, tokens: List<String>): List<String> {
        val root = CommandRegistry.find(label) ?: return emptyList()

        val context = TraversalContext(sender, label, tokens, collectingForSuggestion = true)
        val node = traverse(root, context, suggestionMode = true) ?: return emptyList()

        // Last token partial
        val last = tokens.lastOrNull().orEmpty()

        val childLiterals = node.children
            .filterIsInstance<LiteralNode>()
            .filter { checkPermission(sender, it.permission) }
            .map { it.name }
            .filter { it.startsWith(last, ignoreCase = true) }

        val argumentHints = node.children
            .filterIsInstance<ArgumentNode<*>>()
            .filter { checkPermission(sender, it.permission) }
            .flatMap { argNode ->
                // For tab suggestions we assume CommandArgument has suggestions(prefix, context)
                argNode.argument.suggestions(last, CommandArgument.Context(sender))
            }

        return (childLiterals + argumentHints).distinct()
    }

    // --------------------------------------------------
    // Internal traversal logic
    // --------------------------------------------------

    private fun traverse(
        current: SommandNode,
        ctx: TraversalContext,
        suggestionMode: Boolean = false
    ): SommandNode? {
        if (ctx.index >= ctx.tokens.size) {
            return current
        }

        val token = ctx.tokens[ctx.index]

        // Try literal child match
        current.children.filterIsInstance<LiteralNode>().forEach { lit ->
            if (lit.name.equals(token, ignoreCase = true)) {
                if (!checkPermission(ctx.sender, lit.permission)) {
                    if (!suggestionMode) {
                        ctx.sender.sendMessage(compat.noPermissionMessage())
                    }
                    return null
                }
                ctx.index++
                return traverse(lit, ctx, suggestionMode)
            }
        }

        // Try argument child match
        current.children.filterIsInstance<ArgumentNode<*>>().forEach { argNode ->
            if (!checkPermission(ctx.sender, argNode.permission)) {
                if (!suggestionMode) {
                    ctx.sender.sendMessage(compat.noPermissionMessage())
                }
                return null
            }

            val raw = token
            val parsed = argNode.argument.parse(raw, CommandArgument.Context(ctx.sender))
            if (parsed.isFailure) {
                if (!suggestionMode) {
                    ctx.sender.sendMessage(
                        compat.argumentParseFailedMessage(parsed.error ?: "Invalid")
                    )
                }
                return null
            }

            ctx.argumentsCollected[argNode.argument.id] = parsed.value
            ctx.index++
            return traverse(argNode, ctx, suggestionMode)
        }

        // No match
        return null
    }

    private fun checkPermission(sender: CommandSender, permission: String?): Boolean {
        if (permission.isNullOrBlank()) return true
        return sender.hasPermission(permission)
    }

    // --------------------------------------------------
    // Helper data container (no extension functions used)
    // --------------------------------------------------

    /**
     * Traversal context storing state while descending the tree.
     */
    private data class TraversalContext(
        val sender: CommandSender,
        val label: String,
        val tokens: List<String>,
        var index: Int = 0,
        val argumentsCollected: MutableMap<String, Any?> = mutableMapOf(),
        val collectingForSuggestion: Boolean = false
    )
}

/**
 * Minimal inferred dispatcher interface.
 * Adjust if the real repository interface is different.
 */
interface SommandDispatcher {
    fun dispatch(sender: org.bukkit.command.CommandSender, label: String, tokens: List<String>): Boolean
    fun suggest(sender: org.bukkit.command.CommandSender, label: String, tokens: List<String>): List<String>
}