package com.github.emykr.compat

import com.github.emykr.SommandDispatcher
import com.github.emykr.impl.DefaultDispatcher

/**
 * BaseCompat는 Bukkit/Paper 서버 버전 차이를 숨기기 위한 공통 호환성 베이스 클래스입니다.
 *
 * - 각 버전별 구현체는 serverVersion을 통해 자신이 대상으로 하는
 *   서버 버전을 식별할 수 있습니다.
 * - SommandAPI 내부에서 로직이 필요해질 경우, 이 클래스를 통해
 *   Dispatcher / 메시지 포맷 / 기타 버전 의존 로직을 분리할 수 있습니다.
 *
 * 현재는 기본 Dispatcher와 표준 메시지 유틸만 제공합니다.
 */
abstract class BaseCompat {

    /**
     * 이 Compat 구현이 대상으로 하는 서버 버전입니다.
     * 예: "1.21.4", "1.21.9", "1.21.10"
     */
    abstract val serverVersion: String

    /**
     * 현재 버전에서 사용할 SommandDispatcher를 생성합니다.
     *
     * 기본 구현은 공용 DefaultDispatcher를 반환합니다.
     * 만약 버전별로 다른 디스패처를 사용하고 싶다면 이 메서드를 override 하십시오.
     */
    open fun createDispatcher(): SommandDispatcher {
        // 공용 기본 디스패처 사용
        return DefaultDispatcher()
    }

    /**
     * 알 수 없는 서브 커맨드에 대한 기본 에러 메시지를 반환합니다.
     *
     * @param rootLabel 루트 커맨드 라벨 (예: "user", "plugin" 등)
     */
    open fun unknownSubcommandMessage(rootLabel: String): String {
        // README에 명시된 권장 문구를 기본값으로 사용
        return "Unknown subcommand. Use /$rootLabel help"
    }

    /**
     * 권한이 부족할 때 표준 메시지를 반환합니다.
     */
    open fun noPermissionMessage(): String {
        return "You do not have permission."
    }

    /**
     * 인자 파싱 실패 시 사용할 메시지를 생성합니다.
     *
     * @param reason 파싱 실패 상세 사유 (null 또는 공백일 수도 있음)
     */
    open fun argumentParseFailedMessage(reason: String?): String {
        val safeReason = reason?.takeIf { it.isNotBlank() } ?: "Invalid argument."
        return "Failed to parse argument: $safeReason"
    }

    /**
     * 커맨드 실행 중 예외가 발생했을 때 사용자에게 보여줄 표준 메시지입니다.
     * 실제 스택 트레이스 출력/로그는 플러그인 쪽에서 처리해야 합니다.
     */
    open fun executionErrorMessage(): String {
        return "An internal error occurred while attempting to perform this command."
    }
}