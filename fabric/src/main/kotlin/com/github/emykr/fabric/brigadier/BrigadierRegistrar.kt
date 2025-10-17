package com.github.emykr.fabric.brigadier

import com.github.emykr.CommandRegistry
import com.github.emykr.SommandDispatcher
import com.github.emykr.impl.DefaultDispatcher
import com.github.emykr.node.ArgumentNode
import com.github.emykr.node.LiteralNode
import com.github.emykr.node.SommandNode
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionProvider
import java.util.concurrent.CompletableFuture

/**
 * Registers all commands from CommandRegistry into a Brigadier dispatcher.
 *
 * The caller must provide a sourceMapper that converts the Brigadier command source (S)
 * into the library SommandSource.
 */
object BrigadierRegistrar {

    fun <S> registerAll(
        dispatcher: CommandDispatcher<S>,
        sourceMapper: (S) -> com.github.emykr.SommandSource,
        sommandDispatcher: SommandDispatcher = DefaultDispatcher()
    ) {
        CommandRegistry.allDistinct().forEach { root ->
            val rootBuilder = buildForNode(root, sommandDispatcher, sourceMapper)
            dispatcher.register(rootBuilder)
        }
    }

    private fun <S> buildForNode(
        node: SommandNode,
        sommandDispatcher: SommandDispatcher,
        sourceMapper: (S) -> com.github.emykr.SommandSource
    ): LiteralArgumentBuilder<S> {
        val litBuilder: LiteralArgumentBuilder<S> = LiteralArgumentBuilder.literal<S>(node.name)

        litBuilder.executes { ctx -> executeBrigadier(ctx, node, sommandDispatcher, sourceMapper) }
        litBuilder.suggests(createSuggestionProvider(node, sommandDispatcher, sourceMapper))

        node.children.forEach { child ->
            val childBuilder: ArgumentBuilder<S, *> = when (child) {
                is LiteralNode -> buildForNode(child, sommandDispatcher, sourceMapper)
                is ArgumentNode -> buildForArgument(child, sommandDispatcher, sourceMapper)
                else -> buildForNode(child, sommandDispatcher, sourceMapper)
            }
            litBuilder.then(childBuilder)
        }

        return litBuilder
    }

    private fun <S> buildForArgument(
        argNode: ArgumentNode,
        sommandDispatcher: SommandDispatcher,
        sourceMapper: (S) -> com.github.emykr.SommandSource
    ): RequiredArgumentBuilder<S, String> {
        val argName = argNode.arg.name
        val argType = if (argNode.greedy) StringArgumentType.greedyString() else StringArgumentType.word()
        val req: RequiredArgumentBuilder<S, String> = RequiredArgumentBuilder.argument(argName, argType)

        req.executes { ctx -> executeBrigadier(ctx, argNode, sommandDispatcher, sourceMapper) }
        req.suggests(createSuggestionProvider(argNode, sommandDispatcher, sourceMapper))

        argNode.children.forEach { child ->
            val childBuilder: ArgumentBuilder<S, *> = when (child) {
                is LiteralNode -> buildForNode(child, sommandDispatcher, sourceMapper)
                is ArgumentNode -> buildForArgument(child, sommandDispatcher, sourceMapper)
                else -> buildForNode(child, sommandDispatcher, sourceMapper)
            }
            req.then(childBuilder)
        }

        return req
    }

    private fun <S> executeBrigadier(
        ctx: CommandContext<S>,
        node: SommandNode,
        sommandDispatcher: SommandDispatcher,
        sourceMapper: (S) -> com.github.emykr.SommandSource
    ): Int {
        return try {
            val input = ctx.input.trim()
            val parts = if (input.isEmpty()) emptyList() else input.split(Regex("\\s+"))
            val label = parts.firstOrNull() ?: node.name
            val tokens = if (parts.size <= 1) emptyList() else parts.subList(1, parts.size)

            val source = sourceMapper(ctx.source)
            val success = sommandDispatcher.dispatch(source = source, label = label, tokens = tokens, root = node)
            if (success) 1 else 0
        } catch (ex: CommandSyntaxException) {
            0
        }
    }

    private fun <S> createSuggestionProvider(
        node: SommandNode,
        sommandDispatcher: SommandDispatcher,
        sourceMapper: (S) -> com.github.emykr.SommandSource
    ): SuggestionProvider<S> = SuggestionProvider { _ctx, builder ->
        try {
            // Use SuggestionsBuilder to determine input before the current argument
            val before = builder.input.substring(0, builder.start).trimEnd()
            val parts = if (before.isEmpty()) emptyList() else before.split(Regex("\\s+"))
            val label = parts.firstOrNull() ?: node.name
            val tokens = if (parts.size <= 1) emptyList() else parts.subList(1, parts.size)

            val source = sourceMapper(_ctx.source)
            val suggestions = sommandDispatcher.suggest(source = source, label = label, tokens = tokens, root = node)
            suggestions.forEach { suggestion -> builder.suggest(suggestion) }
            builder.buildFuture()
        } catch (ex: Exception) {
            CompletableFuture.completedFuture(Suggestions.empty())
        }
    }
}
