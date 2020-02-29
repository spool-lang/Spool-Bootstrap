package spool

class Scope(private val parent: Scope? = null) {
    private val internal = mutableListOf<String>()

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
        if (index == -1) throw Exception()
        if (foo && parent != null) index += parent.size()
        return index
    }

    fun size(): Int {
        return internal.size
    }
}