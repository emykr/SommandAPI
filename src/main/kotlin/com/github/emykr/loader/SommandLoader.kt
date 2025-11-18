package com.github.emykr.loader

import org.bukkit.plugin.java.JavaPlugin
import com.github.emykr.dsl.SommandBuilder

/**
 *
 *
 * Sommand DSL 의 최상위 엔트리 포인트.
 *
 * 사용 예:
 * sommand(this) {
 *     simple("ping") {
 *         sender.sendMessage("Pong!")
 *     }
 * }
 */

class SommandLoader(private val plugin: JavaPlugin) {

    /**
     * Loads and registers commands declared inside the DSL block.
     */
    fun load(block: SommandBuilder.() -> Unit) {
        val builder = SommandBuilder(plugin)
        builder.block()
    }
}
//fun sommand(plugin: JavaPlugin, block: SommandBuilder.() -> Unit) {
//    // SommandImpl 에게 실제 로딩을 위임.
//    SommandImpl.load(plugin, block)
//}