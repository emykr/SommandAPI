package com.github.emykr.dsl

import org.bukkit.plugin.java.JavaPlugin
import com.github.emykr.CommandArgument
import com.github.emykr.CommandRegistry
import com.github.emykr.node.ArgumentNode
import com.github.emykr.node.LiteralNode
import com.github.emykr.node.RootNode
import com.github.emykr.node.SommandNode

/**
 * DSL entry builder for defining one or multiple commands.
 *
 * 이 클래스는 "명령 트리 정의"에만 집중합니다.
 * 실제 로딩/엔트리 포인트는 SommandLoader.kt 에서 담당합니다.
 */
class SommandBuilder internal constructor(
    private val plugin: JavaPlugin
) {

    /**
     * 루트 커맨드를 선언하고 즉시 등록합니다.
     */
    fun command(
        vararg aliases: String,
        description: String? = null,
        permission: String? = null,
        block: CommandTreeBuilder.() -> Unit
    ) {
        require(aliases.isNotEmpty()) { "At least one alias must be provided." }
        val main = aliases.first()
        val root = RootNode(main, description, permission, aliases.toList())
        val treeBuilder = CommandTreeBuilder(root)
        treeBuilder.block()
        CommandRegistry.add(root)
    }

    /**
     * 간단한 단일 실행 커맨드 정의 헬퍼.
     */
    fun simple(
        vararg aliases: String,
        description: String? = null,
        permission: String? = null,
        executes: CommandExecution = {}
    ) {
        command(*aliases, description = description, permission = permission) {
            executes(executes)
        }
    }
}

/**
 * SommandNode.ExecutionScope 에 바인딩된 실행 블록 타입.
 */
typealias CommandExecution = SommandNode.ExecutionScope.() -> Unit

/**
 * 루트 하나에 대한 내부 트리 빌더.
 */
class CommandTreeBuilder internal constructor(
    private val current: SommandNode
) {

    /**
     * 리터럴(서브 커맨드) 노드 추가.
     */
    fun literal(
        name: String,
        description: String? = null,
        permission: String? = null,
        block: CommandTreeBuilder.() -> Unit
    ) {
        val lit = LiteralNode(name, description, permission)
        current.children += lit
        CommandTreeBuilder(lit).block()
    }

    /**
     * 실행 가능한 리터럴 노드 추가.
     */
    fun literalExec(
        name: String,
        description: String? = null,
        permission: String? = null,
        executes: CommandExecution
    ) {
        val lit = LiteralNode(name, description, permission)
        lit.executor = executes
        current.children += lit
    }

    /**
     * 인자 노드 추가.
     */
    fun <T : Any> argument(
        arg: CommandArgument<T>,
        description: String? = null,
        permission: String? = null,
        greedy: Boolean = false,
        block: CommandTreeBuilder.() -> Unit
    ) {
        val an = ArgumentNode(arg, description, permission, greedy)
        current.children += an
        CommandTreeBuilder(an).block()
    }

    /**
     * 실행 가능한 인자 노드 추가.
     */
    fun <T : Any> argumentExec(
        arg: CommandArgument<T>,
        description: String? = null,
        permission: String? = null,
        greedy: Boolean = false,
        executes: CommandExecution
    ) {
        val an = ArgumentNode(arg, description, permission, greedy)
        an.executor = executes
        current.children += an
    }

    /**
     * 현재 노드를 실행 지점으로 표시.
     */
    fun executes(block: CommandExecution) {
        current.executor = block
    }

    /**
     * 그룹용 슈가 (실제로는 literal 과 동일).
     */
    fun group(name: String, block: CommandTreeBuilder.() -> Unit) {
        literal(name, block = block)
    }
}