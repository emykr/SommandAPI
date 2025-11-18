package com.github.emykr.compat.v1_21_10

import com.github.emykr.compat.BaseCompat

/**
 * Minecraft/Paper 1.21.10용 호환성 구현입니다.
 *
 * 현재 SommandAPI는 Bukkit API 수준 기능만 사용하므로
 * 다른 버전과의 실제 동작 차이는 없습니다.
 * 필요 시 이 클래스에서 버전 전용 로직을 override 하십시오.
 */
class VersionCompat : BaseCompat() {

    /**
     * 이 Compat가 대상으로 하는 서버 버전.
     */
    override val serverVersion: String = "1.21.10"
}