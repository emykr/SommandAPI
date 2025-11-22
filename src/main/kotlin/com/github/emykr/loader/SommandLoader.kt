package com.github.emykr.loader

import org.bukkit.plugin.java.JavaPlugin
import com.github.emykr.compat.BaseCompat
import com.github.emykr.compat.CompatResolver
import com.github.emykr.dsl.SommandBuilder
import com.github.emykr.dispatcher.CompatAwareDispatcher
import com.github.emykr.dispatcher.SommandDispatcher

/**
 * Responsible for instantiating the DSL builder and executing the user-provided block.
 * Now creates a compat-aware dispatcher automatically.
 */
class SommandLoader private constructor(
    private val plugin: JavaPlugin,
    private val compat: BaseCompat,
    private val dispatcher: SommandDispatcher
) {

    constructor(plugin: JavaPlugin) : this(
        plugin,
        CompatResolver.resolve(),
        CompatAwareDispatcher(CompatResolver.resolve())
    )

    constructor(plugin: JavaPlugin, explicitCompat: BaseCompat) : this(
        plugin,
        explicitCompat,
        CompatAwareDispatcher(explicitCompat)
    )

    fun load(block: SommandBuilder.() -> Unit) {
        val builder = SommandBuilder(plugin, compat)
        builder.block()
        // (추가 지점) 추후 여기서 dispatcher 를 Bukkit CommandMap 에 실제 연결하는 코드 삽입 가능
    }

    fun getCompat(): BaseCompat = compat
    fun getDispatcher(): SommandDispatcher = dispatcher
}