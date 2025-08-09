package com.github.emykr

import com.github.emykr.node.SommandNode

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