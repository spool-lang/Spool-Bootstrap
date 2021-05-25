package spool

import java.util.Stack

class StaticBinder: AstVisitor<Unit> {
    private var currentScope: Scope = Scope(null)
    private val scopeStack: Stack<Scope> = Stack()

    override fun visitFile(file: AstNode.FileNode) {
        file.statements.values.forEach { it.visit(this) }
    }

    override fun visitClass(clazz: AstNode.TypeNode) {
        clazz.properties.forEach { it.visit(this) }
        clazz.constructors.forEach { it.visit(this) }
        clazz.functions.forEach { it.visit(this) }
    }

    override fun visitVariable(variable: AstNode.VariableNode) {
        variable.initializer?.visit(this)
        currentScope.addVariable(variable)
    }

    override fun visitFunction(function: AstNode.FunctionNode) {
        val newScope = Scope(currentScope)

        scopeStack.add(currentScope)
        currentScope = newScope

        function.params.forEach { currentScope.addVariable(it.first, it.second) }
        function.body.forEach { it.visit(this) }

        currentScope = scopeStack.pop()
    }

    override fun visitGenericFunction(genericFunction: AstNode.GenericFunctionNode) {
        TODO("Not yet implemented")
    }

    override fun visitConstructor(constructor: AstNode.ConstructorNode) {
        val newScope = Scope(currentScope)

        scopeStack.add(currentScope)
        currentScope = newScope

        constructor.params.forEach { currentScope.addVariable(it.first, it.second) }
        constructor.body.forEach { it.visit(this) }

        currentScope = scopeStack.pop()
    }

    override fun visitBlock(block: AstNode.BlockNode) {
        block.statements.forEach { it.visit(this) }
    }

    override fun visitIfStatement(ifStatement: AstNode.IfNode) {
        ifStatement.condition.visit(this)
        ifStatement.statements.forEach { it.visit(this) }
        ifStatement.then?.visit(this)
    }

    override fun visitLoop(loop: AstNode.LoopNode) {
        loop.condition?.visit(this)
        loop.incremented?.visit(this)
        loop.incrementer?.visit(this)
        loop.body.forEach { it.visit(this) }
    }

    override fun visitJump(jump: AstNode.JumpNode) {
        return
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
        assignment.value.visit(this)
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
                variable.node!!
            }
            else -> {
                throw Exception()
            }
        }

        get.targetField = type.properties.first { it.name == get.name }
        get.targetFunction = type.functions.first { it.name == get.name }
    }

    override fun visitSet(set: AstNode.SetNode) {
        set.source.visit(this)
        set.value.visit(this)
    }

    override fun visitBinary(binary: AstNode.BinaryNode) {
        binary.left.visit(this)
        binary.right.visit(this)
    }

    override fun visitUnary(unary: AstNode.UnaryNode) {
        unary.source.visit(this)
    }

    override fun visitLiteral(literal: AstNode.LiteralNode) {
        return
    }

    private class Scope(private val parent: Scope?) {
        val variables: MutableMap<String, TypeRef> = mutableMapOf()

        fun getVariable(name: String): TypeRef {
            var variable = variables[name]

            if (variable == null && parent != null) variable = parent.getVariable(name)
            if (variable == null) throw Exception()

            return variable
        }

        fun addVariable(variable: AstNode.VariableNode) {
            variables[variable.name] = variable.type
        }

        fun addVariable(name: String, type: TypeRef) {
            variables[name] = type
        }
    }
}