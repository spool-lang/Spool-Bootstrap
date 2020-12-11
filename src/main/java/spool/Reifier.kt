package spool

import java.util.*

class Reifier: AstVisitor<AstNode> {
    private var currentScope = Scope(null)
    private val scopeStack: Stack<Scope> = Stack()

    override fun visitFile(file: AstNode.FileNode): AstNode {
        file.statements.forEach { (_, node) -> node.visit(this) }
        return file
    }

    override fun visitClass(clazz: AstNode.TypeNode): AstNode {
        TODO("Not yet implemented")
    }

    override fun visitVariable(variable: AstNode.VariableNode): AstNode {
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
    }

    override fun visitIfStatement(ifStatement: AstNode.IfNode): AstNode {
        TODO("Not yet implemented")
    }

    override fun visitLoop(loop: AstNode.LoopNode): AstNode {
        TODO("Not yet implemented")
    }

    override fun visitJump(jump: AstNode.JumpNode): AstNode {
        TODO("Not yet implemented")
    }

    override fun visitConstructorCall(constructorCall: AstNode.ConstructorCallNode): AstNode {
        TODO("Not yet implemented")
    }

    override fun visitFunctionCall(functionCall: AstNode.FunctionCallNode): AstNode {
        TODO("Not yet implemented")
    }

    override fun visitGenericFunctionCall(genericFunctionCall: AstNode.GenericFunctionCallNode): AstNode {
        val source = genericFunctionCall.source

        if (source is AstNode.GenericFunctionNode) {
            val typeParams = source.typeParams
            val genericArguments = genericFunctionCall.genericArguments

            if (typeParams.size == genericArguments.size) {
                val newScope = Scope(currentScope)
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
        TODO("Not yet implemented")
    }

    override fun visitAssignment(assignment: AstNode.AssignmentNode): AstNode {
        TODO("Not yet implemented")
    }

    override fun visitGet(get: AstNode.GetNode): AstNode {
        TODO("Not yet implemented")
    }

    override fun visitSet(set: AstNode.SetNode): AstNode {
        TODO("Not yet implemented")
    }

    override fun visitBinary(binary: AstNode.BinaryNode): AstNode {
        binary.left.visit(this)
        binary.right.visit(this)

        TODO("Not yet implemented")
    }

    override fun visitUnary(unary: AstNode.UnaryNode): AstNode {
        unary.source.visit(this)

        TODO("Not yet implemented")
    }

    override fun visitLiteral(literal: AstNode.LiteralNode): AstNode {
        return literal
    }

    class Scope(private val parent: Scope?) {
        private val reifiedTypes: MutableMap<String, TypeRef> = mutableMapOf()

        fun contains(key: String): Boolean {
            var type = reifiedTypes[key]

            if (type == null && parent != null) type = parent[key]

            return type != null
        }

        operator fun get(key: String): TypeRef {
            var type = reifiedTypes[key]

            if (type == null && parent != null) type = parent[key]
            if (type == null) throw Exception()

            return type
        }

        operator fun set(key: String, typeRef: TypeRef) {
            if (reifiedTypes.contains(key)) throw Exception()

            reifiedTypes[key] = typeRef
        }
    }
}