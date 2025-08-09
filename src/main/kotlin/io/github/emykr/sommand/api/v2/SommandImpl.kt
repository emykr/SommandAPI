package io.github.emykr.sommand.api.v2

import org.bukkit.plugin.java.JavaPlugin
import sommand.api.v2.dsl.SommandBuilder
import sommand.api.v2.loader.SommandLoader

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