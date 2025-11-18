package com.github.emykr.compat.v1_21_10

import com.github.emykr.compat.BaseCompat

/**
 * Minecraft/Paper 1.21.10 용 호환성 구현입니다.
 *
 * 현재 SommandAPI 는 버킷 API 수준에서만 동작하므로
 * 버전별 특수 처리는 없고, 버전 식별과 기본 메시지만 제공합니다.
 */
class VersionCompat : BaseCompat() {

    override val serverVersion: String = "1.21.10"

    // 필요한 경우 이 버전만의 Dispatcher 나 메시지를 override 하면 됩니다.
}