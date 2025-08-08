package sommand.api.v2.impl

import sommand.api.v2.AbstractCommandContext
import sommand.api.v2.CommandContext
import sommand.api.v2.SommandDispatcher
import sommand.api.v2.SommandSource
import sommand.api.v2.node.ArgumentNode
import sommand.api.v2.node.LiteralNode
import sommand.api.v2.node.SommandNode

/**
 * 기본 트리 디스패처.
 * 변경 사항:
 *  - CommandContext -> AbstractCommandContext 업캐스팅 가능 구조
 *  - ExecutionScope 에 context 전달
 */
class DefaultDispatcher : SommandDispatcher {

    override fun dispatch(
        source: SommandSource,
        label: String,
        tokens: List<String>,
        root: SommandNode
    ): Boolean {
        val parsed = mutableMapOf<String, Any>()
        val matched = walk(root, source, tokens, 0, parsed) ?: return false
        val executor = matched.executor ?: return false

        val ctx: AbstractCommandContext = CommandContext(source, label, tokens, parsed)

        executor.invoke(
            SommandNode.ExecutionScope(
                parsedArguments = parsed,
                context = ctx,
                trigger = { /* reserved hook */ }
            )
        )
        return true
    }

    private fun walk(
        node: SommandNode,
        source: SommandSource,
        tokens: List<String>,
        index: Int,
        parsed: MutableMap<String, Any>
    ): SommandNode? {
        if (index >= tokens.size) {
            return if (node.hasExecutor()) node else null
        }

        val token = tokens[index]

        // Literal 우선
        val literal = node.children.filterIsInstance<LiteralNode>()
            .firstOrNull { it.name.equals(token, ignoreCase = true) && passesPermission(source, it) }
        if (literal != null) {
            return walk(literal, source, tokens, index + 1, parsed)
        }

        // Argument
        val argNode = node.children.filterIsInstance<ArgumentNode>()
            .firstOrNull { passesPermission(source, it) && parseArg(it, token, parsed, tokens, index) }
        if (argNode != null) {
            val consumed = if (argNode.greedy) tokens.size - index else 1
            return walk(argNode, source, tokens, index + consumed, parsed)
        }

        return if (node.hasExecutor() && index == tokens.size) node else null
    }

    private fun parseArg(
        node: ArgumentNode,
        token: String,
        parsed: MutableMap<String, Any>,
        tokens: List<String>,
        index: Int
    ): Boolean {
        val arg = node.arg
        if (node.greedy) {
            val remainder = tokens.subList(index, tokens.size).joinToString(" ")
            val value = arg.parse(remainder) ?: return false
            parsed[arg.name] = value
            return true
        }
        val value = arg.parse(token) ?: return false
        parsed[arg.name] = value
        return true
    }

    private fun passesPermission(source: SommandSource, node: SommandNode): Boolean {
        val perm = node.permission ?: return true
        return source.sender.hasPermission(perm)
    }

    override fun suggest(
        source: SommandSource,
        label: String,
        tokens: List<String>,
        root: SommandNode
    ): List<String> {
        val parsed = mutableMapOf<String, Any>()
        return complete(root, source, tokens, 0, parsed)
    }

    private fun complete(
        node: SommandNode,
        source: SommandSource,
        tokens: List<String>,
        index: Int,
        parsed: MutableMap<String, Any>
    ): List<String> {
        if (index >= tokens.size) {
            return suggestionsForNode(node, source, "")
        }

        val token = tokens[index]
        if (index == tokens.lastIndex) {
            return suggestionsForNode(node, source, token)
        }

        val lit = node.children.filterIsInstance<LiteralNode>()
            .firstOrNull { it.name.equals(token, ignoreCase = true) && passesPermission(source, it) }
        if (lit != null) return complete(lit, source, tokens, index + 1, parsed)

        val argNode = node.children.filterIsInstance<ArgumentNode>()
            .firstOrNull {
                passesPermission(source, it) && it.arg.parse(token)?.let { value ->
                    parsed[it.arg.name] = value
                    true
                } ?: false
            }
        if (argNode != null) {
            val consumed = if (argNode.greedy) tokens.size - index else 1
            return complete(argNode, source, tokens, index + consumed, parsed)
        }

        return emptyList()
    }

    private fun suggestionsForNode(
        node: SommandNode,
        source: SommandSource,
        prefix: String
    ): List<String> {
        val literals = node.children.filterIsInstance<LiteralNode>()
            .filter { passesPermission(source, it) }
            .map { it.name }
            .filter { it.startsWith(prefix, ignoreCase = true) }

        val arguments = node.children.filterIsInstance<ArgumentNode>()
            .filter { passesPermission(source, it) }
            .flatMap { argNode -> argNode.arg.suggest(prefix) }
            .filter { it.startsWith(prefix, ignoreCase = true) }

        return (literals + arguments).distinct().sortedBy { it.lowercase() }
    }
}