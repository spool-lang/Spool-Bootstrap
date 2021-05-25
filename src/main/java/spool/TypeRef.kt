package spool

open class TypeRef(val name: String, var node: AstNode.TypeNode? = null) {
    open fun resolveType(node: AstNode.TypeNode) {
        if (this.node == null) this.node = node
        else if (this.node != node) throw Exception("Attempted to resolve node type has already been resolved!")
    }
}