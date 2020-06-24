package spool

import java.util.*

@ExperimentalUnsignedTypes
class BytecodeGenerator: AstVisitor<Unit> {
    private var currentChunk: Chunk = Chunk()
    private var currentClazz: Clazz = Clazz("", "", listOf(), listOf(), listOf())
    private var inClazz: Boolean = false
    private var inOuterFunctionScope = false;
    private val scopeStack = Stack<Scope>()
    private var currentScope = Scope()
    private var bytecodeList: MutableList<Bytecode> = mutableListOf()

    fun run(node: AstNode): List<Bytecode> {
        node.visit(this)
        return bytecodeList
    }

    override fun visitFile(file: AstNode.FileNode) {
        for (statement in file.statements.values) {
            statement.visit(this)
        }
    }

    override fun visitClass(clazz: AstNode.TypeNode) {
        inClazz = true
        val constructors: MutableList<Chunk> = mutableListOf()
        val functions: MutableList<Chunk> = mutableListOf()

        for (constructor in clazz.constructors) {
            constructor.visit(this)
            constructors.add(currentChunk)
        }

        for (function in clazz.functions) {
            function.visit(this)
            functions.add(currentChunk)
        }

        currentClazz = Clazz(clazz.name, clazz.superType.canonicalName, listOf(), constructors, functions)
        bytecodeList.add(currentClazz)
        inClazz = false
    }

    override fun visitVariable(variable: AstNode.VariableNode) {
        // TODO: Account for nullability
        variable.initializer!!.visit(this)

        if (currentScope.isDeclared(variable.name)) throw Exception("Variable ${variable.name} is already declared!")

        currentChunk.instructions.add(Instruction(InstructionType.DECLARE, !variable.const))
        currentScope.declare(variable.name)
    }

    override fun visitFunction(function: AstNode.FunctionNode) {
        val newScope = Scope(currentScope)
        scopeStack.push(currentScope)
        currentScope = newScope
        inOuterFunctionScope = true
        currentChunk = Chunk()
        currentChunk.name = function.name
        //TODO: Renamed  this to something indicative of it's actual purpose
        var skipFirstParam = !function.instance

        for (param in function.params) {
            currentScope.declare(param.first)
            if (skipFirstParam) {
                currentChunk.params.add(param.second.canonicalName)
            }
            else {
                skipFirstParam = true
            }
        }

        function.body.visit(this)
        if (!inClazz) bytecodeList.add(currentChunk)
        currentScope = scopeStack.pop()
    }

    override fun visitConstructor(constructor: AstNode.ConstructorNode) {
        val newScope = Scope(currentScope)
        scopeStack.push(currentScope)
        currentScope = newScope
        currentChunk = Chunk()
        currentChunk.name = "constructor"

        currentScope.declare("self")
        for (param in constructor.params) {
            currentScope.declare(param.first)
        }

        constructor.body.visit(this)
        currentScope = scopeStack.pop()
    }

    override fun visitBlock(block: AstNode.BlockNode) {
        val createNewScope = inOuterFunctionScope
        if (createNewScope) {
            val newScope = Scope(currentScope)
            scopeStack.push(currentScope)
            currentScope = newScope
        }

        inOuterFunctionScope = false
        block.statements.forEach { it.visit(this) }

        currentChunk.addInstruction(Instruction(InstructionType.EXIT_BLOCK, currentScope.size().toUShort()))
        if (createNewScope) currentScope = scopeStack.pop()
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
        currentChunk.instructions.add(Instruction(InstructionType.GET, currentScope.indexOf(id.name).toUShort(), false))
    }

    override fun visitAssignment(assignment: AstNode.AssignmentNode) {
        assignment.value.visit(this)
        if (currentScope.isDeclared(assignment.variable)) currentChunk.instructions.add(Instruction(InstructionType.SET, currentScope.indexOf(assignment.variable).toUShort()))
    }

    override fun visitGet(get: AstNode.GetNode) {
        get.source.visit(this)
        currentChunk.names.add(get.name)
        val index = currentChunk.names.indexOf(get.name)
        currentChunk.addInstruction(Instruction(InstructionType.INSTANCE_GET, index.toUShort()))
    }

    override fun visitSet(set: AstNode.SetNode) {
        set.value.visit(this)
        set.source.visit(this)
        currentChunk.names.add(set.name)
        val index = currentChunk.names.indexOf(set.name)
        currentChunk.addInstruction(Instruction(InstructionType.INSTANCE_SET, index.toUShort()))
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