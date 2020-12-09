package spool

class Reifier: AstVisitor<AstNode?> {
    override fun visitFile(file: AstNode.FileNode): AstNode? {
        file.statements.forEach { (_, node) -> node.visit(this) }
        return null
    }

    override fun visitClass(clazz: AstNode.TypeNode): AstNode? {
        TODO("Not yet implemented")
    }

    override fun visitVariable(variable: AstNode.VariableNode): AstNode? {
        TODO("Not yet implemented")
    }

    override fun visitFunction(function: AstNode.FunctionNode): AstNode? {
        TODO("Not yet implemented")
    }

    override fun visitGenericFunction(genericFunction: AstNode.GenericFunctionNode): AstNode? {
        TODO("Not yet implemented")
    }

    override fun visitConstructor(constructor: AstNode.ConstructorNode): AstNode? {
        TODO("Not yet implemented")
    }

    override fun visitBlock(block: AstNode.BlockNode): AstNode? {
        TODO("Not yet implemented")
    }

    override fun visitIfStatement(ifStatement: AstNode.IfNode): AstNode? {
        TODO("Not yet implemented")
    }

    override fun visitLoop(loop: AstNode.LoopNode): AstNode? {
        TODO("Not yet implemented")
    }

    override fun visitJump(jump: AstNode.JumpNode): AstNode? {
        TODO("Not yet implemented")
    }

    override fun visitConstructorCall(constructorCall: AstNode.ConstructorCallNode): AstNode? {
        TODO("Not yet implemented")
    }

    override fun visitFunctionCall(functionCall: AstNode.FunctionCallNode): AstNode? {
        TODO("Not yet implemented")
    }

    override fun visitGenericFunctionCall(genericFunctionCall: AstNode.GenericFunctionCallNode): AstNode? {
        val source = genericFunctionCall.source

        if (source is AstNode.GenericFunctionNode) {
            if (source.typeParams.size == genericFunctionCall.genericArguments.size) {
                for (i in 0..source.typeParams.size) {
                    
                }
            }
        }

        TODO("Not yet implemented")
    }

    override fun visitID(id: AstNode.IdNode): AstNode? {
        TODO("Not yet implemented")
    }

    override fun visitAssignment(assignment: AstNode.AssignmentNode): AstNode? {
        TODO("Not yet implemented")
    }

    override fun visitGet(get: AstNode.GetNode): AstNode? {
        TODO("Not yet implemented")
    }

    override fun visitSet(set: AstNode.SetNode): AstNode? {
        TODO("Not yet implemented")
    }

    override fun visitBinary(binary: AstNode.BinaryNode): AstNode? {
        binary.left.visit(this)
        binary.right.visit(this)

        return null
    }

    override fun visitUnary(unary: AstNode.UnaryNode): AstNode? {
        unary.source.visit(this)

        return null
    }

    override fun visitLiteral(literal: AstNode.LiteralNode): AstNode? {
        return null
    }

    class Scope(private val parent: Scope?) {
        private val reifiedTypes: MutableMap<String, TypeRef> = mutableMapOf()

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