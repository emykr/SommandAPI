package com.github.emykr.loader

import org.bukkit.plugin.java.JavaPlugin
import com.github.emykr.SommandImpl
import com.github.emykr.compat.BaseCompat
import com.github.emykr.compat.CompatResolver
import com.github.emykr.dsl.SommandBuilder



class SommandLoader(private val plugin: JavaPlugin) {

    /**
     * Loads and registers commands declared inside the DSL block.
     */
    fun load(block: SommandBuilder.() -> Unit) {
        val builder = SommandBuilder(plugin)
        builder.block()
    }
}
