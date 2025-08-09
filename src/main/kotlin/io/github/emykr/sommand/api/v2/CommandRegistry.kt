package io.github.emykr.sommand.api.v2

import io.github.emykr.sommand.api.v2.node.RootNode
import java.util.concurrent.ConcurrentHashMap

/**
 * Holds registered root command nodes.
 */
object CommandRegistry {
    private val roots: MutableMap<String, RootNode> = ConcurrentHashMap()

    fun add(root: RootNode) {
        // Register by main name and record by aliases internally.
        roots[root.name.lowercase()] = root
        root.aliases.forEach { alias ->
            roots.putIfAbsent(alias.lowercase(), root)
        }
    }

    fun get(name: String): RootNode? = roots[name.lowercase()]

    fun allDistinct(): Set<RootNode> = roots.values.toSet()

    fun clear() = roots.clear()
}