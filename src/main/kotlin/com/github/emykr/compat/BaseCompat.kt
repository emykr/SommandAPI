package com.github.emykr.compat

import com.github.emykr.SommandDispatcher
import com.github.emykr.impl.DefaultDispatcher

/**
 * BaseCompat 는 버킷/페이퍼 버전 차이를 흡수하기 위한
 * 공통 호환성 인터페이스입니다.
 *
 * 현재는 Dispatcher, 메시지 포맷 정도만 추상화하고 있으며
 * 이후 버전별로 NMS 사용이나 Paper만 제공하는 기능을 쓴다면
 * 해당 구현체 안에서만 처리하면 됩니다.
 */
abstract class BaseCompat {

    /**
     * 이 API가 동작하는 서버 버전 문자열입니다.
     * 예: "1.21.4", "1.21.9" 등.
     */
    abstract val serverVersion: String

    /**
     * 현재 버전에서 사용할 디스패처 구현을 반환합니다.
     *
     * 기본적으로는 공용 DefaultDispatcher 를 사용하지만,
     * 버전별로 탭완성/권한 처리/예외 메시지 포맷 등을
     * 다르게 하고 싶다면 이 메서드만 재정의하면 됩니다.
     */
    open fun createDispatcher(): SommandDispatcher {
        // 기본 구현은 공용 DefaultDispatcher 사용
        return DefaultDispatcher()
    }

    /**
     * 알 수 없는 서브 커맨드에 대한 기본 에러 메시지를 제공합니다.
     */
    open fun unknownSubcommandMessage(rootLabel: String): String {
        // README 권장 문구를 기본값으로 사용
        return "Unknown subcommand. Use /$rootLabel help"
    }

    /**
     * 권한 부족 시 표준 메시지를 제공합니다.
     */
    open fun noPermissionMessage(): String {
        return "You do not have permission."
    }

    /**
     * 인자 파싱 실패 시 사용할 메시지를 생성합니다.
     *
     * @param reason 파싱 실패 상세 사유 (nullable 안전 처리)
     */
    open fun argumentParseFailedMessage(reason: String?): String {
        val detail = reason?.takeIf { it.isNotBlank() } ?: "Invalid argument."
        return "Failed to parse argument: $detail"
    }

    /**
     * 예외 발생 시 플레이어에게 보여줄 표준화된 메시지입니다.
     * 실제 예외 로그 출력은 플러그인 쪽에서 처리하십시오.
     */
    open fun executionErrorMessage(): String {
        return "An internal error occurred while attempting to perform this command."
    }
}