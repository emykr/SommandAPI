package io.github.emykr.sommand.api.v2

/**
 * Abstract base for command execution context.
 *
 * 목표:
 * 1) 다형성: 다른 형태의 컨텍스트(예: PlayerCommandContext, ConsoleCommandContext) 도입 가능.
 * 2) 공통 API 계약: source / label / rawArgs / get() / all().
 * 3) 확장성: Decorator 혹은 Proxy 컨텍스트로 감싸 기능(Audit, Metrics 등) 추가 가능.
 */
abstract class AbstractCommandContext {

    /**
     * 명령 실행 주체 추상화.
     */
    abstract val source: SommandSource

    /**
     * 실제 실행된 루트 명령 라벨(타이핑한 alias 포함).
     */
    abstract val label: String

    /**
     * 공백 기준 토큰화된 원본 인자.
     */
    abstract val rawArgs: List<String>

    /**
     * 특정 인자명으로 파싱된 값을 얻기 (타입 체크 포함).
     * 잘못된 타입이거나 존재하지 않으면 IllegalArgumentException.
     */
    abstract fun <T : Any> get(name: String, type: Class<T>): T

    /**
     * Reified 편의 함수.
     */
    inline fun <reified T : Any> get(name: String): T = get(name, T::class.java)

    /**
     * 모든 파싱된 인자 스냅샷 (불변 Map).
     */
    abstract fun all(): Map<String, Any>
}