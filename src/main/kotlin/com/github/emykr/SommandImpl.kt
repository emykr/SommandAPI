package com.github.emykr

import com.github.emykr.compat.BaseCompat
import com.github.emykr.loader.SommandLoader
import com.github.emykr.dsl.SommandBuilder
import org.bukkit.plugin.java.JavaPlugin

/**
 * Facade entry point for the library.
 */
object SommandImpl {

    fun load(plugin: JavaPlugin, block: SommandBuilder.() -> Unit) {
        SommandLoader(plugin).load(block)
    }

    fun load(plugin: JavaPlugin, compat: BaseCompat, block: SommandBuilder.() -> Unit) {
        SommandLoader(plugin, compat).load(block)
    }
}