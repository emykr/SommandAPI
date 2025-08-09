package io.github.emykr.sommand.api.v2.node

import sommand.api.v2.AbstractCommandContext
import sommand.api.v2.CommandArgument

/**
 * 명령 트리의 노드 기본 모델.
 * sealed: LiteralNode / ArgumentNode / RootNode
 */
sealed class SommandNode(
    val name: String,
    val description: String?,
    val permission: String?
) {
    internal val children: MutableList<SommandNode> = mutableListOf()
    internal var executor: (ExecutionScope.() -> Unit)? = null
    internal var greedy: Boolean = false
    internal var argument: CommandArgument<*>? = null

    fun hasExecutor(): Boolean = executor != null

    /**
     * 실행 스코프:
     *  - parsedArguments: 파싱된 인자 (기존 호환)
     *  - context: AbstractCommandContext (타입 세이프 접근)
     *  - trigger(): 추후 체이닝 Hook / pipeline 용 예약 (현 시점 NO-OP)
     */
    class ExecutionScope(
        val parsedArguments: MutableMap<String, Any>,
        val context: AbstractCommandContext,
        val trigger: () -> Unit
    )
}

class LiteralNode(
    name: String,
    description: String?,
    permission: String?
) : SommandNode(name, description, permission)

class ArgumentNode(
    val arg: CommandArgument<*>,
    description: String?,
    permission: String?,
    greedy: Boolean = false
) : SommandNode(arg.name, description, permission) {
    init {
        argument = arg
        this.greedy = greedy
    }
}

class RootNode(
    name: String,
    description: String?,
    permission: String?,
    val aliases: List<String>
) : SommandNode(name, description, permission)