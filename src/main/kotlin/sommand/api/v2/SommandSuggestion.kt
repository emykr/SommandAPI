package sommand.api.v2

import java.util.Locale

/**
 * Represents a single tab-completion suggestion.
 *
 * value       : 실제로 탭 확정 시 커맨드 라인에 들어갈 문자열
 * tooltip     : (현재 Bukkit 기본 탭에서는 표시되지 않지만) 향후 Brigadier/Adventure 적용 대비 설명
 * permission  : 이 suggestion 을 노출하기 위한 추가 퍼미션 (노드 퍼미션과 별개, null 이면 무조건 통과)
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

    /**
     * 간단한 문자열 배열을 SommandSuggestion 리스트로 변환.
     */
    fun of(vararg values: String): List<SommandSuggestion> =
            values.map { SimpleSommandSuggestion(it) }

    /**
     * value -> tooltip 맵을 suggestion 리스트로 변환.
     */
    fun withTooltips(map: Map<String, String>): List<SommandSuggestion> =
            map.map { (k, v) -> SimpleSommandSuggestion(k, v) }

    /**
     * 빌더 스타일 생성.
     */
    inline fun build(block: MutableList<SommandSuggestion>.() -> Unit): List<SommandSuggestion> {
        val list = mutableListOf<SommandSuggestion>()
        list.block()
        return list
    }
}

/**
 * prefix / permission 필터링 + 정렬.
 */
fun List<SommandSuggestion>.filterFor(prefix: String, source: SommandSource): List<SommandSuggestion> {
    if (this.isEmpty()) return this
    val lowerPrefix = prefix.toLowerCase(Locale.ROOT)
    return this
        .filter { s ->
            (s.permission == null || source.sender.hasPermission(s.permission)) &&
                    (prefix.isEmpty() || s.value.toLowerCase(Locale.ROOT).startsWith(lowerPrefix))
        }
        .sortedBy { it.value.toLowerCase(Locale.ROOT) }
}

/**
 * SommandSuggestion 리스트를 문자열 값 리스트로 변환.
 */
fun List<SommandSuggestion>.toValues(): List<String> = this.map { it.value }