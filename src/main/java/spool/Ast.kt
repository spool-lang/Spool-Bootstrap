package spool

interface AstVisitor<T> {
    fun visitFile(file: AstNode.FileNode): T

    fun visitClass(clazz: AstNode.TypeNode): T

    fun visitVariable(variable: AstNode.VariableNode): T

    fun visitFunction(function: AstNode.FunctionNode): T

    fun visitGenericFunction(genericFunction: AstNode.GenericFunctionNode): T

    fun visitConstructor(constructor: AstNode.ConstructorNode): T

    fun visitBlock(block: AstNode.BlockNode): T

    fun visitIfStatement(ifStatement: AstNode.IfNode): T

    fun visitLoop(loop: AstNode.LoopNode): T

    fun visitJump(jump: AstNode.JumpNode): T

    fun visitConstructorCall(constructorCall: AstNode.ConstructorCallNode): T

    fun visitFunctionCall(functionCall: AstNode.FunctionCallNode): T

    fun visitGenericFunctionCall(genericFunctionCall: AstNode.GenericFunctionCallNode): T

    fun visitID(id: AstNode.IdNode): T

    fun visitAssignment(assignment: AstNode.AssignmentNode): T

    fun visitGet(get: AstNode.GetNode): T

    fun visitSet(set: AstNode.SetNode): T

    fun visitBinary(binary: AstNode.BinaryNode): T

    fun visitUnary(unary: AstNode.UnaryNode): T

    fun visitLiteral(literal: AstNode.LiteralNode): T
}

sealed class AstNode {
    abstract fun <T> visit(visitor: AstVisitor<T>): T

    class FileNode(val statements: Map<String, AstNode>, val namespace: String, val imports: List<Import>): AstNode() {
        override fun <T> visit(visitor: AstVisitor<T>): T {
            return visitor.visitFile(this)
        }
    }

    class TypeNode(val name: String, val native: Boolean = false, val superType: TypeRef? = null, val properties: List<VariableNode> = listOf(), val constructors: List<ConstructorNode> = listOf(), val functions: List<FunctionNode> = listOf()): AstNode() {
        override fun <T> visit(visitor: AstVisitor<T>): T {
            return visitor.visitClass(this)
        }

        fun isOrSubtypeOf(other: TypeNode): Boolean {
            if (name == other.name) return true

            if (other.superType?.node != null) {
                return isOrSubtypeOf(other.superType.node!!)
            }

            return false
        }
    }

    class VariableNode(val name: String, val type: TypeRef, val const: Boolean, val initializer: AstNode?): AstNode() {
        override fun <T> visit(visitor: AstVisitor<T>): T {
            return visitor.visitVariable(this)
        }
    }

    class FunctionNode(val name: String, val params: List<Pair<String, TypeRef>>, val body: List<AstNode>, val instance: Boolean = false): AstNode() {
        override fun <T> visit(visitor: AstVisitor<T>): T {
            return visitor.visitFunction(this)
        }
    }

    class GenericFunctionNode(val name: String, val body: List<AstNode>, val typeParams: List<String>, val params: List<Pair<String, TypeRef>>, val instance: Boolean = false): AstNode() {
        val reified: MutableList<FunctionNode> = mutableListOf()

        override fun <T> visit(visitor: AstVisitor<T>): T {
            return visitor.visitGenericFunction(this)
        }
    }

    class ConstructorNode(val body: List<AstNode>, val params: List<Pair<String, TypeRef>>): AstNode() {
        override fun <T> visit(visitor: AstVisitor<T>): T {
            return visitor.visitConstructor(this)
        }

    }

    class BlockNode(val statements: List<AstNode>): AstNode() {
        override fun <T> visit(visitor: AstVisitor<T>): T {
            return visitor.visitBlock(this)
        }
    }

    class IfNode(val condition: AstNode, val statements: List<AstNode>, val then: AstNode?): AstNode() {
        override fun <T> visit(visitor: AstVisitor<T>): T {
            return visitor.visitIfStatement(this)
        }
    }

    class LoopNode(val condition: AstNode?, val incremented: AstNode?, val incrementer: AstNode?, val body: List<AstNode>): AstNode() {
        override fun <T> visit(visitor: AstVisitor<T>): T {
            return visitor.visitLoop(this)
        }
    }

    // Only for loops
    class JumpNode(val next: Boolean): AstNode() {
        override fun <T> visit(visitor: AstVisitor<T>): T {
            return visitor.visitJump(this)
        }
    }

    class ConstructorCallNode(var type: TypeRef, val arguments: List<AstNode>): AstNode() {
        override fun <T> visit(visitor: AstVisitor<T>): T {
            return visitor.visitConstructorCall(this)
        }
    }

    class FunctionCallNode(val source: AstNode, val arguments: List<AstNode>): AstNode() {
        override fun <T> visit(visitor: AstVisitor<T>): T {
            return visitor.visitFunctionCall(this)
        }
    }

    class GenericFunctionCallNode(val source: AstNode, val genericArguments: List<TypeRef>, val arguments: List<AstNode>): AstNode() {
        lateinit var reifiedSource: AstNode

        override fun <T> visit(visitor: AstVisitor<T>): T {
            return visitor.visitGenericFunctionCall(this)
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

    class SetNode(val name: String, val source: AstNode, val value: AstNode): AstNode() {
        override fun <T> visit(visitor: AstVisitor<T>): T {
            return visitor.visitSet(this)
        }
    }

    class AssignmentNode(val variable: String, val value: AstNode): AstNode() {
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