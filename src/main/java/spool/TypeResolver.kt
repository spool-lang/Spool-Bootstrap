package spool

class TypeResolver(val db: FileDB) : AstVisitor<Unit> {
    var imports: List<Import> = listOf()

    fun resolve(node: AstNode) {
        node.visit(this)
    }

    override fun visitFile(file: AstNode.FileNode) {
        imports = file.imports
        file.statements.forEach { t, u -> u.visit(this) }
    }

    override fun visitClass(clazz: AstNode.TypeNode) {
        val superTypeNode = getType(clazz.superType!!.name)
        clazz.superType.resolveType(superTypeNode)

        clazz.properties.forEach { it.visit(this) }
        clazz.constructors.forEach { it.visit(this) }
        clazz.functions.forEach { it.visit(this) }
    }

    override fun visitVariable(variable: AstNode.VariableNode) {
        val typeNode = getType(variable.type.name)
        variable.type.resolveType(typeNode)

        if (variable.initializer != null) {
            variable.initializer.visit(this)
        }
    }

    override fun visitFunction(function: AstNode.FunctionNode) {
        for (param in function.params) {
            val typeNode = getType(param.second.name)
            param.second.resolveType(typeNode)
        }

        function.body.forEach { it.visit(this) }
    }

    override fun visitGenericFunction(genericFunction: AstNode.GenericFunctionNode) {
        genericFunction.reified.forEach { it.visit(this) }
    }

    override fun visitConstructor(constructor: AstNode.ConstructorNode) {
        for (param in constructor.params) {
            val typeNode = getType(param.second.name)
            param.second.resolveType(typeNode)
        }

        constructor.body.forEach { it.visit(this) }
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
        val typeNode = getType(constructorCall.type.name)
        constructorCall.type.resolveType(typeNode)
        constructorCall.arguments.forEach { it.visit(this) }
    }

    override fun visitFunctionCall(functionCall: AstNode.FunctionCallNode) {
        functionCall.source.visit(this)
        functionCall.arguments.forEach { it.visit(this) }
    }

    override fun visitGenericFunctionCall(genericFunctionCall: AstNode.GenericFunctionCallNode) {
        genericFunctionCall.genericArguments.forEach { it.resolveType(getType(it.name)) }
        genericFunctionCall.source.visit(this)
        genericFunctionCall.arguments.forEach { it.visit(this) }
    }

    override fun visitID(id: AstNode.IdNode) {
        return
    }

    override fun visitAssignment(assignment: AstNode.AssignmentNode) {
        assignment.value.visit(this)
    }

    override fun visitGet(get: AstNode.GetNode) {
        get.source.visit(this)
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

    private fun getType(name: String): AstNode.TypeNode {
        val filtered = imports.filter { it.endsWith(name) }

        if (filtered.isNotEmpty()) {
            return db.getTypeNode(filtered[0].getName())
        }

        return db.getTypeNode(name)
    }
}