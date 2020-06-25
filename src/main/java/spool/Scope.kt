package spool

@ExperimentalUnsignedTypes
class Scope(private val parent: Scope? = null) {
    private val internal = mutableListOf<String>()
    private var endJumpPoint: JumpPoint? = null
    private var startJumpPoint: JumpPoint? = null
    public var inLoop: Boolean = false

    fun declare(name: String) {
        if (internal.contains(name)) throw Exception("Variable $name is already declared!")
        internal.add(name)
    }

    fun isDeclared(name: String): Boolean {
        if (internal.contains(name)) return true
        else if (parent != null) return parent.isDeclared(name)
        return false
    }

    fun indexOf(name: String): Int {
        var index = internal.indexOf(name)
        var foo = true
        if (index == -1 && parent != null) {
            index = parent.indexOf(name)
            foo = false
        }
        if (index == -1) throw Exception("Variable $name is not in scope!")
        if (foo && parent != null) index += parent.size()
        return index
    }

    fun size(): Int {
        return internal.size
    }

    fun getEndJumpPoint(): JumpPoint {
        return if (endJumpPoint != null) endJumpPoint!! else parent!!.getEndJumpPoint()
    }

    fun createEndJumpPoint(): JumpPoint {
        if (this.endJumpPoint == null) this.endJumpPoint = JumpPoint()
        return getEndJumpPoint()
    }

    fun getStartJumpPoint(): JumpPoint {
        return if (startJumpPoint != null) startJumpPoint!! else parent!!.getStartJumpPoint()
    }

    fun createStartJumpPoint(): JumpPoint {
        if (this.startJumpPoint == null) this.startJumpPoint = JumpPoint()
        return getStartJumpPoint()
    }

    fun isInLoop(): Boolean {
        return inLoop || parent?.inLoop ?: false
    }
}