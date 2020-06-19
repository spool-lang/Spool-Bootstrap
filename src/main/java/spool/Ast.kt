package spool

interface AstVisitor<T> {
    fun visitFile(file: AstNode.FileNode): T

    fun visitClass(clazz: AstNode.TypeNode): T

    fun visitVariable(variable: AstNode.VariableNode): T

    fun visitFunction(function: AstNode.FunctionNode): T

    fun visitBlock(block: AstNode.BlockNode): T

    fun visitConstructorCall(constructorCall: AstNode.ConstructorCallNode): T

    fun visitFunctionCall(functionCall: AstNode.FunctionCallNode): T

    fun visitID(id: AstNode.IdNode): T

    fun visitAssignment(assignment: AstNode.AssignmentNode): T

    fun visitGet(get: AstNode.GetNode): T

    fun visitBinary(binary: AstNode.BinaryNode): T

    fun visitUnary(unary: AstNode.UnaryNode): T

    fun visitLiteral(literal: AstNode.LiteralNode): T
}

data class Type(val canonicalName: String, var node: AstNode.TypeNode? = null) {
    fun resolveType(node: AstNode.TypeNode) {
        if (this.node != null) this.node = node
        else throw Exception("Attempted to resolve node type has already been resolved!")
    }
}

sealed class AstNode {

    abstract fun <T> visit(visitor: AstVisitor<T>): T

    class FileNode(val statements: Map<String, AstNode>, val namespace: String, val imports: Map<String, String>): AstNode() {
        override fun <T> visit(visitor: AstVisitor<T>): T {
            return visitor.visitFile(this)
        }
    }

    class TypeNode(val name: String, val superType: Type, val fields: List<VariableNode>): AstNode() {
        override fun <T> visit(visitor: AstVisitor<T>): T {
            return visitor.visitClass(this)
        }
    }

    class VariableNode(val name: String, val type: Type, val const: Boolean, val initializer: AstNode?): AstNode() {
        override fun <T> visit(visitor: AstVisitor<T>): T {
            return visitor.visitVariable(this)
        }
    }

    class FunctionNode(val name: String, val body: List<AstNode>, val params: List<Pair<String, Type>>): AstNode() {
        override fun <T> visit(visitor: AstVisitor<T>): T {
            return visitor.visitFunction(this)
        }
    }

    class BlockNode(val statements: List<AstNode>): AstNode() {
        override fun <T> visit(visitor: AstVisitor<T>): T {
            return visitor.visitBlock(this)
        }
    }

    class ConstructorCallNode(val typeName: String, val arguments: List<AstNode>): AstNode() {
        override fun <T> visit(visitor: AstVisitor<T>): T {
            return visitor.visitConstructorCall(this)
        }
    }

    class FunctionCallNode(val source: AstNode, val arguments: List<AstNode>): AstNode() {
        override fun <T> visit(visitor: AstVisitor<T>): T {
            return visitor.visitFunctionCall(this)
        }
    }

    class IdNode(val name: String): AstNode() {
        override fun <T> visit(visitor: AstVisitor<T>): T {
            return visitor.visitID(this)
        }
    }

    class GetNode(val name: String, val source: AstNode): AstNode() {
        override fun <T> visit(visitor: AstVisitor<T>): T {
            return visitor.visitGet(this)
        }
    }

    class AssignmentNode(val name: String, val source: AstNode): AstNode() {
        override fun <T> visit(visitor: AstVisitor<T>): T {
            return visitor.visitAssignment(this)
        }
    }

    class BinaryNode(val left: AstNode, val operator: Token, val right: AstNode): AstNode() {
        override fun <T> visit(visitor: AstVisitor<T>): T {
            return visitor.visitBinary(this)
        }
    }

    class UnaryNode(val source: AstNode, val operator: Token): AstNode() {
        override fun <T> visit(visitor: AstVisitor<T>): T {
            return visitor.visitUnary(this)
        }
    }

    class LiteralNode(val literal: Any): AstNode() {
        override fun <T> visit(visitor: AstVisitor<T>): T {
            return visitor.visitLiteral(this)
        }
    }
}