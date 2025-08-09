package io.github.emykr.sommand.api.v2

import io.github.emykr.sommand.api.v2.node.SommandNode

/**
 * Dispatcher interface so alternative dispatch strategies can be added.
 */
interface SommandDispatcher {
    /**
     * Executes the appropriate command branch using tokens and returns true if execution succeeded.
     */
    fun dispatch(
        source: SommandSource,
        label: String,
        tokens: List<String>,
        root: SommandNode
    ): Boolean

    /**
     * Tab completion suggestions for current raw tokens.
     */
    fun suggest(
        source: SommandSource,
        label: String,
        tokens: List<String>,
        root: SommandNode
    ): List<String>
}