package spool

open class TypeRef(val name: String, var node: AstNode.TypeNode? = null, private val constraints: List<TypeRef> = listOf()) {
    open fun resolveType(node: AstNode.TypeNode) {
        if (this.node == null) this.node = node
        else throw Exception("Attempted to resolve node type has already been resolved!")
    }

    fun fitsConstraints(): Boolean {
        if (constraints.size > 1) {
            TODO("interfaces are not yet implemented.")
        }

        if (constraints.isEmpty()) {
            return true
        }

        return constraints[0] === node?.superType
    }
}