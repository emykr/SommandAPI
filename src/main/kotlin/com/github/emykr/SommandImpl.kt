package com.github.emykr

import com.github.emykr.dsl.SommandBuilder
import org.bukkit.plugin.java.JavaPlugin
import com.github.emykr.loader.SommandLoader

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