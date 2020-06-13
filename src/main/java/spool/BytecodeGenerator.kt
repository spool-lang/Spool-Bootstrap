package spool

import java.util.*

class BytecodeGenerator: AstVisitor<Unit> {
    private var currentChunk = Chunk()
    private val scopeStack = Stack<Scope>()
    private var currentScope = Scope()

    fun run(node: AstNode): Bytecode {
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

        if (currentScope.isDeclared(variable.name)) throw Exception()

        currentChunk.instructions.add(Instruction(InstructionType.DECLARE, !variable.const))
        currentScope.declare(variable.name)
    }

    override fun visitFunction(function: AstNode.FunctionNode) {
        currentChunk.name = function.name

        for (param in function.params) {
            currentScope.declare(param.first)
            currentChunk.params.add(param.second.canonicalName)
        }

        for (node in function.body) {
            node.visit(this)
        }
    }

    override fun visitBlock(block: AstNode.BlockNode) {
        val newScope = Scope(currentScope)
        scopeStack.push(currentScope)
        currentScope = newScope

        block.statements.forEach { it.visit(this) }

        currentChunk.addInstruction(Instruction(InstructionType.EXIT_BLOCK, currentScope.size().toUShort()))
        currentScope = scopeStack.pop()
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

            val index = currentChunk.names.indexOf(functionCall.source.name)
            if (index == -1) {
                currentChunk.instructions.add(Instruction(InstructionType.CALL_INSTANCE, currentChunk.names.size.toUShort()))
                currentChunk.names.add(functionCall.source.name)
            }
            else {
                currentChunk.instructions.add(Instruction(InstructionType.CALL_INSTANCE, index.toUShort()))
            }
        }
        else TODO()
    }

    override fun visitID(id: AstNode.IdNode) {
        if (currentScope.isDeclared(id.name)) currentChunk.instructions.add(Instruction(InstructionType.GET, currentScope.indexOf(id.name).toUShort(), false))
    }

    override fun visitAssignment(assignment: AstNode.AssignmentNode) {
        assignment.source.visit(this)
        if (currentScope.isDeclared(assignment.name)) currentChunk.instructions.add(Instruction(InstructionType.SET, currentScope.indexOf(assignment.name).toUShort()))
    }

    override fun visitGet(get: AstNode.GetNode) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitBinary(binary: AstNode.BinaryNode) {
        binary.right.visit(this)
        binary.left.visit(this)

        val op = when (binary.operator.type) {
            TokenType.PLUS -> InstructionType.ADD
            TokenType.MINUS -> InstructionType.SUBTRACT
            TokenType.MULTIPLY -> InstructionType.MULTIPLY
            TokenType.DIVIDE -> InstructionType.DIVIDE
            TokenType.POW -> InstructionType.POWER
            TokenType.LESS -> InstructionType.LESS
            TokenType.GREATER -> InstructionType.GREATER
            TokenType.LESS_EQUAL -> InstructionType.LESS_EQ
            TokenType.GREATER_EQUAL -> InstructionType.GREATER_EQ
            TokenType.NOT_EQUAL -> InstructionType.NOT_EQ
            TokenType.EQUAL -> InstructionType.EQ
            TokenType.AND -> InstructionType.AND
            TokenType.OR -> InstructionType.OR
            else -> throw Exception()
        }

        currentChunk.addInstruction(Instruction(op))
    }

    override fun visitUnary(unary: AstNode.UnaryNode) {
        unary.source.visit(this)

        val op = when (unary.operator.type) {
            TokenType.MINUS -> InstructionType.NUM_NEGATE
            TokenType.NOT -> InstructionType.LOGIC_NEGATE
            else -> throw Exception()
        }

        currentChunk.addInstruction(Instruction(op))
    }

    override fun visitLiteral(literal: AstNode.LiteralNode) {
        currentChunk.addConstant(literal.literal)
        currentChunk.instructions.add(Instruction(InstructionType.GET, currentChunk.constants.indexOf(literal.literal).toUShort(), true))
    }
}