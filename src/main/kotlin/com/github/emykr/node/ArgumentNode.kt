package com.github.emykr.node

/**
 * This file intentionally left without a separate ArgumentNode class definition to avoid
 * redeclaration conflicts.
 *
 * The real implementation of:
 *  - sealed class SommandNode
 *  - class ArgumentNode(...)
 *  - class LiteralNode(...)
 *  - class RootNode(...)
 *
 * lives in SommandNode.kt within the same package.
 *
 * Keeping this file (instead of deleting) preserves any historical imports or references,
 * while preventing a duplicate class definition that caused:
 *  - Redeclaration: class ArgumentNode
 *  - "Too many arguments for constructor" errors
 *
 * If you previously imported `sommand.api.v2.node.ArgumentNode`, it will now correctly
 * resolve to the implementation inside SommandNode.kt.
 */
@Suppress("unused")
internal object ArgumentNodeFileMarker