package spool

import java.util.Stack

class StaticBinder: AstVisitor<Unit> {
    private var currentScope: Scope = Scope(null)
    private val scopeStack: Stack<Scope> = Stack()

    override fun visitFile(file: AstNode.FileNode) {
        TODO("Not yet implemented")
    }

    override fun visitClass(clazz: AstNode.TypeNode) {
        TODO("Not yet implemented")
    }

    override fun visitVariable(variable: AstNode.VariableNode) {
        variable.initializer?.visit(this)
        currentScope.addVariable(variable)
    }

    override fun visitFunction(function: AstNode.FunctionNode) {
        TODO("Not yet implemented")
    }

    override fun visitGenericFunction(genericFunction: AstNode.GenericFunctionNode) {
        TODO("Not yet implemented")
    }

    override fun visitConstructor(constructor: AstNode.ConstructorNode) {
        TODO("Not yet implemented")
    }

    override fun visitBlock(block: AstNode.BlockNode) {
        TODO("Not yet implemented")
    }

    override fun visitIfStatement(ifStatement: AstNode.IfNode) {
        TODO("Not yet implemented")
    }

    override fun visitLoop(loop: AstNode.LoopNode) {
        TODO("Not yet implemented")
    }

    override fun visitJump(jump: AstNode.JumpNode) {
        TODO("Not yet implemented")
    }

    override fun visitConstructorCall(constructorCall: AstNode.ConstructorCallNode) {
        TODO("Not yet implemented")
    }

    override fun visitFunctionCall(functionCall: AstNode.FunctionCallNode) {
        TODO("Not yet implemented")
    }

    override fun visitGenericFunctionCall(genericFunctionCall: AstNode.GenericFunctionCallNode) {
        TODO("Not yet implemented")
    }

    override fun visitID(id: AstNode.IdNode) {
        return
    }

    override fun visitAssignment(assignment: AstNode.AssignmentNode) {
        TODO("Not yet implemented")
    }

    override fun visitGet(get: AstNode.GetNode) {
        val source = get.source
        source.visit(this)

        val type = when (source) {
            is AstNode.GetNode -> {
                val targetField = source.targetField!!
                targetField.type.node!!
            }
            is AstNode.IdNode -> {
                val variable = currentScope.getVariable(source.name)
                variable.type.node!!
            }
            else -> {
                throw Exception()
            }
        }

        get.targetField = type.properties.first { it.name == get.name }
        get.targetFunction = type.functions.first { it.name == get.name }
    }

    override fun visitSet(set: AstNode.SetNode) {
        TODO("Not yet implemented")
    }

    override fun visitBinary(binary: AstNode.BinaryNode) {
        TODO("Not yet implemented")
    }

    override fun visitUnary(unary: AstNode.UnaryNode) {
        TODO("Not yet implemented")
    }

    override fun visitLiteral(literal: AstNode.LiteralNode) {
        TODO("Not yet implemented")
    }

    private class Scope(private val parent: Scope?) {
        val variables: MutableMap<String, AstNode.VariableNode> = mutableMapOf()

        fun getVariable(name: String): AstNode.VariableNode {
            var variable = variables[name]

            if (variable == null && parent != null) variable = parent.getVariable(name)
            if (variable == null) throw Exception()

            return variable
        }

        fun addVariable(variable: AstNode.VariableNode) {
            variables[variable.name] = variable
        }
    }
}