package spool

import blue.endless.jankson.*

class AstPrinter: AstVisitor<JsonElement> {
    fun printAst(node: AstNode): String {
        return node.visit(this).toJson(false, true)
    }

    override fun visitFile(file: AstNode.FileNode): JsonElement {
        TODO("Not implemented!")
    }

    override fun visitClass(clazz: AstNode.TypeNode): JsonElement {
        TODO("Not implemented!")
    }

    override fun visitVariable(variable: AstNode.VariableNode): JsonElement {
        val json = JsonObject();

        json["node"] = JsonPrimitive("variable")
        json["name"] = JsonPrimitive(variable.name)
        json["const"] = JsonPrimitive(variable.const)
        json["type"] = JsonPrimitive(variable.type.canonicalName)
        variable.initializer?.let { json["initializer"] = it.visit(this) }

        return json
    }

    override fun visitFunction(function: AstNode.FunctionNode): JsonElement {
        val json = JsonObject();

        json["node"] = JsonPrimitive("function")
        json["name"] = JsonPrimitive(function.name)
        val paramsArray = JsonArray()
        for (param in function.params) {
            paramsArray.add(JsonPrimitive("${param.first}:${param.second.canonicalName}"))
        }
        json["params"] = paramsArray

        val bodyArray = JsonArray()
        for (node in function.body) {
            bodyArray.add(node.visit(this))
        }
        json["body"] = bodyArray

        return json
    }

    override fun visitBlock(block: AstNode.BlockNode): JsonElement {
        val json = JsonObject()

        json["node"] = JsonPrimitive("block")
        val statements = JsonArray()
        block.statements.forEach {statements.add(it.visit(this))}
        json["statements"] = statements

        return json
    }

    override fun visitConstructorCall(constructorCall: AstNode.ConstructorCallNode): JsonElement {
        val json = JsonObject()

        json["node"] = JsonPrimitive("constructor call")
        json["type"] = JsonPrimitive(constructorCall.typeName)
        val args = JsonArray()
        for (arg in constructorCall.arguments) {
            args.add(arg.visit(this))
        }
        json["args"] = args

        return json
    }

    override fun visitFunctionCall(functionCall: AstNode.FunctionCallNode): JsonElement {
        val json = JsonObject()

        json["node"] = JsonPrimitive("function call")
        val args = JsonArray()
        for (arg in functionCall.arguments) {
            args.add(arg.visit(this))
        }
        json["args"] = args
        json["source"] = functionCall.source.visit(this)

        return json
    }

    override fun visitID(id: AstNode.IdNode): JsonElement {
        val json = JsonObject()

        json["node"] = JsonPrimitive("id")
        json["id"] = JsonPrimitive(id.name)

        return json
    }

    override fun visitGet(get: AstNode.GetNode): JsonElement {
        val json = JsonObject()

        json["node"] = JsonPrimitive("get")
        json["name"] = JsonPrimitive(get.name)
        json["source"] = get.source.visit(this)

        return json
    }

    override fun visitAssignment(assignment: AstNode.AssignmentNode): JsonElement {
        val json = JsonObject()

        json["node"] = JsonPrimitive("assignment")
        json["target"] = JsonPrimitive(assignment.name)
        json["source"] = assignment.source.visit(this)

        return json
    }

    override fun visitBinary(binary: AstNode.BinaryNode): JsonElement {
        val json = JsonObject()

        json["node"] = JsonPrimitive("binary")
        json["operator"] = JsonPrimitive(binary.operator.lexeme!!)
        json["left"] = binary.left.visit(this)
        json["right"] = binary.right.visit(this)

        return json
    }

    override fun visitUnary(unary: AstNode.UnaryNode): JsonElement {
        val json = JsonObject()

        json["node"] = JsonPrimitive("unary")
        json["operator"] = JsonPrimitive(unary.operator.lexeme!!)
        json["source"] = unary.source.visit(this)

        return json
    }

    override fun visitLiteral(literal: AstNode.LiteralNode): JsonElement {
        val json = JsonObject()

        json["node"] = JsonPrimitive("literal")
        json["literal"] = JsonPrimitive(literal.literal)

        return json
    }
}