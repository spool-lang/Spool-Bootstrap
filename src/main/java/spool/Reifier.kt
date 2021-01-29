package spool

import java.util.*

class Reifier: AstVisitor<AstNode> {
    private var currentScope = Scope()
    private val scopeStack: Stack<Scope> = Stack()

    override fun visitFile(file: AstNode.FileNode): AstNode {
        file.statements.forEach { (_, node) -> node.visit(this) }
        return file
    }

    override fun visitClass(clazz: AstNode.TypeNode): AstNode {
        TODO("Not yet implemented")
    }

    override fun visitVariable(variable: AstNode.VariableNode): AstNode {
        val initializer = variable.initializer?.visit(this)
        val typeName = variable.type.name

        if (currentScope.contains(typeName)) {
            return AstNode.VariableNode(variable.name, currentScope[typeName], variable.const, initializer)
        }

        return variable
    }

    override fun visitFunction(function: AstNode.FunctionNode): AstNode {
        TODO("Not yet implemented")
    }

    override fun visitGenericFunction(genericFunction: AstNode.GenericFunctionNode): AstNode {
        val newName = "${genericFunction.name}\$${genericFunction.reified.size}"

        val newParams = mutableListOf<Pair<String, TypeRef>>()
        for (param in genericFunction.params) {
            if (currentScope.contains(param.second.name)) {
                newParams.add(param.first to currentScope[param.second.name])
            }
            else {
                newParams.add(param)
            }
        }

        val newBody = genericFunction.body.map { it.visit(this) }

        val reifiedFunction = AstNode.FunctionNode(newName, newParams, newBody, genericFunction.instance)
        genericFunction.reified.add(reifiedFunction)
        return reifiedFunction
    }

    override fun visitConstructor(constructor: AstNode.ConstructorNode): AstNode {
        TODO("Not yet implemented")
    }

    override fun visitBlock(block: AstNode.BlockNode): AstNode {
        if (currentScope.isEmpty()) {
            block.statements.forEach { it.visit(this) }
            return block
        }

        return AstNode.BlockNode(block.statements.map { it.visit(this) })
    }

    override fun visitIfStatement(ifStatement: AstNode.IfNode): AstNode {
        if (currentScope.isEmpty()) {
            ifStatement.condition.visit(this)
            ifStatement.statements.forEach { it.visit(this) }
            ifStatement.then?.visit(this)
            return ifStatement
        }

        return AstNode.IfNode(ifStatement.condition.visit(this), ifStatement.statements.map { it.visit(this) }, ifStatement.then?.visit(this))
    }

    override fun visitLoop(loop: AstNode.LoopNode): AstNode {
        if (currentScope.isEmpty()) {
            loop.condition?.visit(this)
            loop.incremented?.visit(this)
            loop.incrementer?.visit(this)
            loop.body.forEach { it.visit(this) }
            return loop
        }

        return AstNode.LoopNode(loop.condition?.visit(this), loop.incremented?.visit(this), loop.incrementer?.visit(this), loop.body.map { it.visit(this) })
    }

    override fun visitJump(jump: AstNode.JumpNode): AstNode {
        return jump
    }

    override fun visitConstructorCall(constructorCall: AstNode.ConstructorCallNode): AstNode {
        TODO("Not yet implemented")
    }

    override fun visitFunctionCall(functionCall: AstNode.FunctionCallNode): AstNode {
        val newScope = Scope()

        if (currentScope.isEmpty()) {
            functionCall.arguments.forEach { it.visit(this) }

            scopeStack.push(currentScope)
            currentScope = newScope

            functionCall.source.visit(this)

            currentScope = scopeStack.pop()

            return functionCall
        }

        val reifiedArguments = functionCall.arguments.map { it.visit(this) }

        scopeStack.push(currentScope)
        currentScope = newScope

        functionCall.source.visit(this)

        return AstNode.FunctionCallNode(functionCall.source, reifiedArguments)
    }

    override fun visitGenericFunctionCall(genericFunctionCall: AstNode.GenericFunctionCallNode): AstNode {
        val source = genericFunctionCall.source

        if (source is AstNode.GenericFunctionNode) {
            val typeParams = source.typeParams
            val genericArguments = genericFunctionCall.genericArguments

            // Technically a semantics check, but not checking it here may cause issues.
            if (typeParams.size == genericArguments.size) {
                val newScope = Scope()
                scopeStack.push(currentScope)
                currentScope = newScope

                for (i in 0..typeParams.size) {
                    currentScope[typeParams[i]] = genericArguments[i]
                }

                source.visit(this)

                currentScope = scopeStack.pop()
            }
        }

        TODO("Not yet implemented")
    }

    override fun visitID(id: AstNode.IdNode): AstNode {
        return id
    }

    override fun visitAssignment(assignment: AstNode.AssignmentNode): AstNode {
        if (currentScope.isEmpty()) {
            assignment.value.visit(this)
            return assignment
        }

        return AstNode.AssignmentNode(assignment.variable, assignment.value.visit(this))
    }

    override fun visitGet(get: AstNode.GetNode): AstNode {
        if (currentScope.isEmpty()) {
            get.source.visit(this)
            return get
        }

        return AstNode.AssignmentNode(get.name, get.source.visit(this))
    }

    override fun visitSet(set: AstNode.SetNode): AstNode {
        if (currentScope.isEmpty()) {
            set.source.visit(this)
            set.value.visit(this)
            return set
        }

        return AstNode.SetNode(set.name, set.source.visit(this), set.value.visit(this))
    }

    override fun visitBinary(binary: AstNode.BinaryNode): AstNode {
        if (currentScope.isEmpty()) {
            binary.left.visit(this)
            binary.right.visit(this)
            return binary
        }

        return AstNode.BinaryNode(binary.left.visit(this), binary.operator, binary.right.visit(this))
    }

    override fun visitUnary(unary: AstNode.UnaryNode): AstNode {
        if (currentScope.isEmpty()) {
            unary.source.visit(this)
            return unary
        }

        return AstNode.UnaryNode(unary.source.visit(this), unary.operator)
    }

    override fun visitLiteral(literal: AstNode.LiteralNode): AstNode {
        return literal
    }

    class Scope {
        private val reifiedTypes: MutableMap<String, TypeRef> = mutableMapOf()

        fun isEmpty(): Boolean {
            return reifiedTypes.isEmpty()
        }

        fun contains(key: String): Boolean {
            return reifiedTypes[key] != null
        }

        operator fun get(key: String): TypeRef {
            return reifiedTypes[key] ?: throw Exception()
        }

        operator fun set(key: String, typeRef: TypeRef) {
            if (reifiedTypes.contains(key)) throw Exception()

            reifiedTypes[key] = typeRef
        }
    }
}