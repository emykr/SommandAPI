package com.github.emykr

/**
 * 기본 구현 컨텍스트.
 * 추가 커스텀 컨텍스트(예: Player 전용)를 만들고 싶다면 AbstractCommandContext 상속 클래스를 별도로 정의한 뒤
 * Dispatcher 를 커스터마이징하거나 Hook 지점을 만들어 교체할 수 있음.
 */
class CommandContext(
    override val source: SommandSource,
    override val label: String,
    override val rawArgs: List<String>,
    private val parsed: Map<String, Any>
) : AbstractCommandContext() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> get(name: String, type: Class<T>): T {
        val value = parsed[name]
            ?: throw IllegalArgumentException("Argument '$name' was not provided.")
        if (!type.isInstance(value)) {
            throw IllegalArgumentException("Argument '$name' expected ${type.simpleName} but got ${value::class.java.simpleName}.")
        }
        return value as T
    }


    override fun all(): Map<String, Any> = parsed.toMap()
}