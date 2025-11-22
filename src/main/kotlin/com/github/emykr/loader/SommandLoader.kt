package com.github.emykr.loader

import org.bukkit.plugin.java.JavaPlugin
import com.github.emykr.compat.BaseCompat
import com.github.emykr.compat.CompatResolver
import com.github.emykr.dsl.SommandBuilder

/**
 * Responsible for instantiating the DSL builder and executing the user-provided block.
 *
 * Provides two modes:
 * 1) Automatic compat resolution (default constructor)
 * 2) Explicit compat injection (secondary constructor)
 */
class SommandLoader private constructor(
    private val plugin: JavaPlugin,
    private val compat: BaseCompat
) {

    /**
     * Primary factory: resolves compat automatically.
     */
    constructor(plugin: JavaPlugin) : this(plugin, CompatResolver.resolve())

    /**
     * Secondary factory: explicit compat override.
     */
    constructor(plugin: JavaPlugin, explicitCompat: BaseCompat) : this(plugin, explicitCompat)

    /**
     * Loads and registers commands declared inside the DSL block using resolved compat.
     */
    fun load(block: SommandBuilder.() -> Unit) {
        val builder = SommandBuilder(plugin, compat)
        builder.block()
        // If future steps require compat-driven post-processing (e.g. dispatcher registration),
        // this is the centralized place to add it.
    }

    /**
     * Exposes the compat used in this loader.
     */
    fun getCompat(): BaseCompat = compat
}