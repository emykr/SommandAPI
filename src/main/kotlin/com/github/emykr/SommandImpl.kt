package com.github.emykr

import com.github.emykr.compat.BaseCompat
import com.github.emykr.loader.SommandLoader
import com.github.emykr.dsl.SommandBuilder
import org.bukkit.plugin.java.JavaPlugin

/**
 * Facade entry point for the library.
 * Provides overloads with and without explicit compat injection.
 */
object SommandImpl {

    /**
     * Loads commands with automatically resolved compat.
     */
    fun load(plugin: JavaPlugin, block: SommandBuilder.() -> Unit) {
        SommandLoader(plugin).load(block)
    }

    /**
     * Loads commands with an explicit compat instance.
     */
    fun load(plugin: JavaPlugin, compat: BaseCompat, block: SommandBuilder.() -> Unit) {
        SommandLoader(plugin, compat).load(block)
    }
}