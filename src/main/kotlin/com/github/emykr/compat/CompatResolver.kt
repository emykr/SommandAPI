package com.github.emykr.compat

import com.github.emykr.compat.v1_21_4.VersionCompat as Compat_1_21_4
import com.github.emykr.compat.v1_21_9.VersionCompat as Compat_1_21_9
import com.github.emykr.compat.v1_21_10.VersionCompat as Compat_1_21_10
import org.bukkit.Bukkit

/**
 * 서버 버전을 감지하고 적절한 BaseCompat 구현을 선택하는 유틸리티입니다.
 *
 * - 우선 Bukkit.getBukkitVersion() 또는 Bukkit.getVersion() 에서
 *   "1.21.4", "1.21.9", "1.21.10" 등의 패턴을 파싱합니다.
 * - 대응되는 VersionCompat 구현이 있으면 그 인스턴스를 반환합니다.
 * - 없으면 FallbackCompat (기본 구현) 을 사용합니다.
 */
object CompatResolver {

    /**
     * 현재 서버 환경에 맞는 BaseCompat 구현을 반환합니다.
     * 항상 null이 아닌 유효한 인스턴스를 돌려줍니다.
     */
    fun resolve(): BaseCompat {
        val versionString = extractServerVersion()
        return when (versionString) {
            "1.21.4" -> Compat_1_21_4()
            "1.21.9" -> Compat_1_21_9()
            "1.21.10" -> Compat_1_21_10()
            else -> FallbackCompat(versionString)
        }
    }

    /**
     * Bukkit에서 노출하는 버전 문자열에서 "주요.부.패치" 형태만 추출합니다.
     *
     * 예시 입력:
     *  - "1.21.4-R0.1-SNAPSHOT" (getBukkitVersion)
     *  - "git-Paper-123 (MC: 1.21.9)" (getVersion)
     *
     * 실패 시 "unknown" 을 반환하지만, 이는 FallbackCompat에서 그대로 사용됩니다.
     */
    private fun extractServerVersion(): String {
        // 1. getBukkitVersion 시도
        val bukkitVersion = runCatching { Bukkit.getBukkitVersion() }.getOrNull()
        val fromBukkit = parseMCVersion(bukkitVersion)
        if (fromBukkit != null) {
            return fromBukkit
        }

        // 2. getVersion 시도 (예: "git-Paper-123 (MC: 1.21.9)")
        val fullVersion = runCatching { Bukkit.getVersion() }.getOrNull()
        val fromFull = parseMCVersion(fullVersion)
        if (fromFull != null) {
            return fromFull
        }

        // 3. 실패 시 unknown
        return "unknown"
    }

    /**
     * 문자열에서 "숫자.숫자.숫자" 패턴을 찾아 반환합니다.
     * 없으면 null.
     */
    private fun parseMCVersion(source: String?): String? {
        if (source.isNullOrBlank()) {
            return null
        }

        // 정규식: 1.21.4, 1.21.10 등
        val regex = Regex("""\d+\.\d+\.\d+""")
        val match = regex.find(source)
        return match?.value
    }

    /**
     * 알려진 버전에 매칭되지 않을 때 사용하는 기본 Compat 구현입니다.
     *
     * - serverVersion은 파싱된 문자열(또는 "unknown")을 그대로 사용합니다.
     * - 나머지 동작은 BaseCompat의 기본 구현과 동일합니다.
     */
    private class FallbackCompat(override val serverVersion: String) : BaseCompat()
}