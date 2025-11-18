package com.github.emykr

import com.github.emykr.dsl.SommandBuilder
import com.github.emykr.loader.SommandLoader
import org.bukkit.plugin.java.JavaPlugin

/**
 * Facade entry point for the library.
 */
object SommandImpl {

    /**
     * Loads and registers commands declared inside the DSL block.
     */
    fun load(plugin: JavaPlugin, block: SommandBuilder.() -> Unit) {
        SommandLoader(plugin).load(block)
    }
}