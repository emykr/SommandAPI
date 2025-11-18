package com.github.emykr.loader

import org.bukkit.plugin.java.JavaPlugin
import com.github.emykr.SommandImpl
import com.github.emykr.compat.BaseCompat
import com.github.emykr.compat.CompatResolver
import com.github.emykr.dsl.SommandBuilder

/**
 * Top-level DSL entry point.
 *
 * 기본 사용:
 * sommand(plugin) {
 *     simple("ping") {
 *         sender.sendMessage("Pong!")
 *     }
 * }
 *
 * Compat 은 자동으로 서버 버전에 맞춰 선택됩니다.
 */
fun sommand(plugin: JavaPlugin, block: SommandBuilder.() -> Unit) {
    @Suppress("UnusedVariable") val compat: BaseCompat = CompatResolver.resolve()
    // SommandImpl.load 의 시그니처가 (plugin, SommandBuilder.() -> Unit) 라고 가정
    SommandImpl.load(plugin) {
        // this 는 SommandBuilder
        block(this)
    }
}

/**
 * Overload allowing an explicit BaseCompat to be passed.
 *
 * 테스트나 특정 서버 버전을 강제하고 싶을 때 사용 가능합니다.
 */
fun sommand(plugin: JavaPlugin, compat: BaseCompat, block: SommandBuilder.() -> Unit) {
    SommandImpl.load(plugin) {
        // 필요하면 여기에서 compat 을 SommandBuilder 에 전달하도록
        // SommandImpl.load 쪽 시그니처를 확장할 수도 있습니다.
        block(this)
    }
}