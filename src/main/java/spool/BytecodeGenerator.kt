package spool

import java.lang.Exception

class BytecodeGenerator: AstVisitor<Unit> {
    private var currentChunk = Chunk()

    fun run(node: AstNode): Chunk {
        node.visit(this)
        val result = currentChunk
        currentChunk = Chunk()
        return result
    }

    override fun visitFile(file: AstNode.FileNode) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitClass(clazz: AstNode.TypeNode) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitVariable(variable: AstNode.VariableNode) {
        // TODO: Account for nullability
        variable.initializer!!.visit(this)
        currentChunk.instructions.add(Instruction(InstructionType.DECLARE, !variable.const))
        currentChunk.variableNames.add(variable.name)
    }

    override fun visitFunction(function: AstNode.FunctionNode) {
        currentChunk.name = function.name

        for (param in function.params) {
            currentChunk.variableNames.add(param.first)
            currentChunk.params.add(param.second.canonicalName)
        }

        for (node in function.body) {
            node.visit(this)
        }
    }

    override fun visitBlock(block: AstNode.BlockNode) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitConstructorCall(constructorCall: AstNode.ConstructorCallNode) {
        currentChunk.instructions.add(Instruction(InstructionType.GET_TYPE, currentChunk.names.size.toUShort()))
        currentChunk.instructions.add(Instruction(InstructionType.NEW, 0.toUShort()))
        currentChunk.names.add(constructorCall.typeName)
    }

    override fun visitFunctionCall(functionCall: AstNode.FunctionCallNode) {
        if (functionCall.source is AstNode.GetNode) {
            functionCall.arguments.forEach { it.visit(this) }
            functionCall.source.source.visit(this)
            currentChunk.instructions.add(Instruction(InstructionType.CALL_INSTANCE, currentChunk.names.size.toUShort()))
            currentChunk.names.add(functionCall.source.name)
        }
        else TODO()
    }

    override fun visitID(id: AstNode.IdNode) {
        val index = currentChunk.variableNames.indexOf(id.name)

        if (currentChunk.variableNames[index] == id.name) currentChunk.instructions.add(Instruction(InstructionType.GET, index.toUShort(), false))
    }

    override fun visitGet(get: AstNode.GetNode) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitLiteral(literal: AstNode.LiteralNode) {
        currentChunk.instructions.add(Instruction(InstructionType.GET, currentChunk.constants.size.toUShort(), true))
        currentChunk.constants.add(literal.literal)
    }

}