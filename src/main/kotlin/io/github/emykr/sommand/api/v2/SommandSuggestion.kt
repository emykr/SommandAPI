package io.github.emykr.sommand.api.v2

import java.util.Locale

/**
 * Represents a single tab-completion suggestion.
 */
interface SommandSuggestion {
    val value: String
    val tooltip: String?
    val permission: String?
}

/**
 * 기본 구현체.
 */
data class SimpleSommandSuggestion(
    override val value: String,
    override val tooltip: String? = null,
    override val permission: String? = null
) : SommandSuggestion

/**
 * Suggestion 유틸.
 */
object Suggestions {

    fun of(vararg values: String): List<SommandSuggestion> =
            values.map { SimpleSommandSuggestion(it) }

    fun withTooltips(map: Map<String, String>): List<SommandSuggestion> =
            map.map { (k, v) -> SimpleSommandSuggestion(k, v) }

    inline fun build(block: MutableList<SommandSuggestion>.() -> Unit): List<SommandSuggestion> {
        val list = mutableListOf<SommandSuggestion>()
        list.block()
        return list
    }
}

/**
 * prefix / permission 필터링 + 정렬.
 * permission 은 인터페이스 프로퍼티라 스마트 캐스트 불가 -> 지역 변수에 담아서 사용.
 */
fun List<SommandSuggestion>.filterFor(prefix: String, source: SommandSource): List<SommandSuggestion> {
    if (isEmpty()) return this
    val lowerPrefix = prefix.lowercase(Locale.ROOT)
    return this
        .filter { s ->
            val perm = s.permission
            (perm == null || source.sender.hasPermission(perm)) &&
                    (prefix.isEmpty() || s.value.lowercase(Locale.ROOT).startsWith(lowerPrefix))
        }
        .sortedBy { it.value.lowercase(Locale.ROOT) }
}

/**
 * 문자열 값 리스트로 변환.
 */
fun List<SommandSuggestion>.toValues(): List<String> = map { it.value }