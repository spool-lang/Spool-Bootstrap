package spool

import blue.endless.jankson.JsonElement
import java.util.*

class SemanticAnalyzer(private val db: FileDB): AstVisitor<AstNode.TypeNode?> {
    private val imports: MutableList<Import> = mutableListOf()
    private var currentScope = Scope(null)
    private val scopeStack: Stack<Scope> = Stack()

    fun analyze(node: AstNode) {
        node.visit(this)
    }

    override fun visitFile(file: AstNode.FileNode): AstNode.TypeNode? {
        imports.addAll(file.imports)
        file.statements.forEach { (_, node) -> node.visit(this) }
        return null
    }

    override fun visitClass(clazz: AstNode.TypeNode): AstNode.TypeNode? {
        if (clazz.superType != null) {
            try {
                clazz.superType.node!!.visit(this)
            } catch (e: Exception) {
                throw Exception("Inheritance tree for type '${clazz.name}' was either too large or recursive." , e)
            }
        }

        clazz.properties.forEach { it.visit(this) }
        clazz.constructors.forEach { it.visit(this) }
        clazz.functions.forEach { it.visit(this) }

        return null
    }

    override fun visitVariable(variable: AstNode.VariableNode): AstNode.TypeNode? {
        val typeNode = variable.type.node ?: throw Exception()

        if (variable.initializer != null) {
            if (typeNode != variable.initializer.visit(this)) throw Exception()
        }

        currentScope[variable.name] = typeNode
        return null
    }

    override fun visitFunction(function: AstNode.FunctionNode): AstNode.TypeNode? {
        val newScope = Scope(currentScope)
        scopeStack.push(currentScope)
        currentScope = newScope
        function.body.forEach { it.visit(this) }
        currentScope = scopeStack.pop()
        return null
    }

    override fun visitGenericFunction(genericFunction: AstNode.GenericFunctionNode): AstNode.TypeNode? {
        TODO("Not yet implemented")
    }

    override fun visitConstructor(constructor: AstNode.ConstructorNode): AstNode.TypeNode? {
        val newScope = Scope(currentScope)
        scopeStack.push(currentScope)
        currentScope = newScope
        constructor.body.forEach { it.visit(this) }
        currentScope = scopeStack.pop()
        return null
    }

    override fun visitBlock(block: AstNode.BlockNode): AstNode.TypeNode? {
        val newScope = Scope(currentScope)
        scopeStack.push(currentScope)
        currentScope = newScope
        block.statements.forEach { it.visit(this) }
        currentScope = scopeStack.pop()
        return null
    }

    override fun visitIfStatement(ifStatement: AstNode.IfNode): AstNode.TypeNode? {
       if (ifStatement.condition.visit(this) != db.getTypeNode("spool.core.Boolean")) {
           throw Exception("If-statement condition did not resolve to a boolean.")
       }

        val newScope = Scope(currentScope)
        scopeStack.push(currentScope)
        currentScope = newScope
        ifStatement.statements.forEach { it.visit(this) }
        currentScope = scopeStack.pop()
        return null
    }

    override fun visitLoop(loop: AstNode.LoopNode): AstNode.TypeNode? {
        if (loop.condition != null) {
            if (loop.condition.visit(this) != db.getTypeNode("spool.core.Boolean")) {
                throw Exception("If-statement condition did not resolve to a boolean.")
            }
        }

        if (loop.incremented != null) loop.incremented.visit(this)
        if (loop.incrementer != null) loop.incrementer.visit(this)

        val newScope = Scope(currentScope)
        scopeStack.push(currentScope)
        currentScope = newScope
        loop.body.forEach { it.visit(this) }
        currentScope = scopeStack.pop()
        return null
    }

    override fun visitJump(jump: AstNode.JumpNode): AstNode.TypeNode? {
        return null
    }

    override fun visitConstructorCall(constructorCall: AstNode.ConstructorCallNode): AstNode.TypeNode? {
        val type = constructorCall.type
        var resolved = false

        for (constructor in type.node!!.constructors) {
            if (constructor.params.size == constructorCall.arguments.size) {
                resolved = true
                for (pair in pairedList(constructor.params, constructorCall.arguments)) {
                    val argumentType = pair.second.visit(this) ?: throw Exception()

                    if (!argumentType.isOrSubtypeOf(argumentType)) {
                        resolved = false
                        break
                    }
                }
            }
        }

        if (!resolved) throw Exception()

        return type.node
    }

    override fun visitFunctionCall(functionCall: AstNode.FunctionCallNode): AstNode.TypeNode? {
        if (functionCall.source is AstNode.GetNode) {
            val node = functionCall.source.source.visit(this)
            var resolved = false

            for (function in node!!.functions) {
                var params = function.params
                if (params.isNotEmpty() && params[0].first == "self") params = params.subList(1, params.size)

                if (params.size == functionCall.arguments.size) {
                    resolved = true

                    for (pair in pairedList(params, functionCall.arguments)) {
                        val argumentType = pair.second.visit(this) ?: throw Exception()

                        if (!argumentType.isOrSubtypeOf(argumentType)) {
                            resolved = false
                            break
                        }
                    }
                }
            }

            if (!resolved) throw Exception()

            return node
        }

        TODO("Account for global functions.")
    }

    override fun visitGenericFunctionCall(genericFunctionCall: AstNode.GenericFunctionCallNode): AstNode.TypeNode? {
        TODO("Not yet implemented")
    }

    override fun visitID(id: AstNode.IdNode): AstNode.TypeNode? {
        return currentScope[id.name]
    }

    override fun visitAssignment(assignment: AstNode.AssignmentNode): AstNode.TypeNode? {
        val variableType = currentScope[assignment.variable]
        val valueType = assignment.value.visit(this)

        if (valueType != variableType) throw Exception()

        return null
    }

    override fun visitGet(get: AstNode.GetNode): AstNode.TypeNode? {
        val sourceType = get.source.visit(this) ?: throw Exception()

        val filtered = sourceType.properties.filter { it.name == get.name }

        if (filtered.isEmpty()) throw Exception()

        return filtered[0].type.node
    }

    override fun visitSet(set: AstNode.SetNode): AstNode.TypeNode? {
        val sourceType = set.source.visit(this)
        val valueType = set.value.visit(this)

        if (sourceType == null || valueType != sourceType) throw Exception()

        return null
    }

    override fun visitBinary(binary: AstNode.BinaryNode): AstNode.TypeNode? {
        val leftType = binary.left.visit(this)
        val rightType = binary.right.visit(this)

        // TODO Need to check for if the operation is valid + output type.
        if (leftType == null || leftType != rightType) throw Exception()

        // Doesn't matter which one returns since they are both the same.
        return leftType
    }

    override fun visitUnary(unary: AstNode.UnaryNode): AstNode.TypeNode? {
        // TODO: Check the output type of the operator.
        return unary.source.visit(this) ?: throw Exception()
    }

    override fun visitLiteral(literal: AstNode.LiteralNode): AstNode.TypeNode? {
        if (literal.literal is String) return db.getTypeNode("spool.core.String")
        if (literal.literal is Int) return db.getTypeNode("spool.core.Int16")
        if (literal.literal is Boolean) return db.getTypeNode("spool.core.Boolean")
        throw Exception("Unknown literal type!")
    }

    class Scope(private val parent: Scope?) {
        private val variables: MutableMap<String, AstNode.TypeNode> = mutableMapOf()

        operator fun get(key: String): AstNode.TypeNode {
            var type = variables[key]

            if (type == null && parent != null) type = parent[key]
            if (type == null) throw Exception()

            return type
        }

        operator fun set(key: String, variable: AstNode.TypeNode) {
            if (variables.contains(key)) throw Exception()

            variables[key] = variable
        }
    }
}