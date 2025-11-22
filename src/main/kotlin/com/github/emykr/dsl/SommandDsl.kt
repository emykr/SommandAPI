package com.github.emykr.dsl

import com.github.emykr.SommandImpl
import com.github.emykr.compat.BaseCompat
import org.bukkit.plugin.java.JavaPlugin

/**
 * Top-level DSL entry convenience function.
 */
fun sommand(plugin: JavaPlugin, block: SommandBuilder.() -> Unit) {
    SommandImpl.load(plugin, block)
}

fun sommand(plugin: JavaPlugin, compat: BaseCompat, block: SommandBuilder.() -> Unit) {
    SommandImpl.load(plugin, compat, block)
}