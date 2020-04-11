package com.android.tools.diagnostics

/** A call tree, represented recursively. */
interface CallTree {
    val tracepoint: Tracepoint
    val callCount: Long
    val wallTime: Long
    val children: Map<Tracepoint, CallTree>

    val selfWallTime: Long
        get() = wallTime - children.values.sumByLong(CallTree::wallTime)

    fun allNodesInSubtree(): Sequence<CallTree> {
        return sequenceOf(this) + children.values.asSequence().flatMap(CallTree::allNodesInSubtree)
    }

    /** Returns an immutable deep copy of this tree. */
    fun snapshot(): CallTree {
        return ImmutableCallTree(
            tracepoint = tracepoint,
            callCount = callCount,
            wallTime = wallTime,
            children = children.mapValues { (_, child) -> child.snapshot() }
        )
    }
}

/** An immutable call tree implementation. */
class ImmutableCallTree(
    override val tracepoint: Tracepoint,
    override val callCount: Long,
    override val wallTime: Long,
    override val children: Map<Tracepoint, CallTree>
) : CallTree

/** A mutable call tree implementation. */
class MutableCallTree(
    override val tracepoint: Tracepoint
) : CallTree {
    override var callCount: Long = 0
    override var wallTime: Long = 0
    override val children: MutableMap<Tracepoint, MutableCallTree> = LinkedHashMap()

    /** Accumulates the data from another call tree into this one. */
    fun accumulate(other: CallTree) {
        require(other.tracepoint == tracepoint) {
            "Doesn't make sense to sum call tree nodes representing different tracepoints"
        }

        callCount += other.callCount
        wallTime += other.wallTime

        for ((childTracepoint, otherChild) in other.children) {
            val child = children.getOrPut(childTracepoint) { MutableCallTree(childTracepoint) }
            child.accumulate(otherChild)
        }
    }

    fun clear() {
        callCount = 0
        wallTime = 0
        children.values.forEach(MutableCallTree::clear)
    }
}
