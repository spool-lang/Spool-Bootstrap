package spool

data class Type(val name: String, var node: AstNode.TypeNode? = null) {
    fun resolveType(node: AstNode.TypeNode) {
        if (this.node == null) this.node = node
        else throw Exception("Attempted to resolve node type has already been resolved!")
    }
}