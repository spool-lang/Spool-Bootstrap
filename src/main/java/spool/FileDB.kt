package spool


class FileDB {
    val map: MutableMap<String, AstNode> = mutableMapOf()

    init {
        map["spool.core.Object"] = AstNode.TypeNode("spool.core.Object", native = true)
        map["spool.core.Boolean"] = AstNode.TypeNode("spool.core.Boolean", true, TypeRef("spool.core.object"))
        map["spool.core.Char"] = AstNode.TypeNode("spool.core.Char", true, TypeRef("spool.core.object"))
        map["spool.core.String"] = AstNode.TypeNode("spool.core.String", true, TypeRef("spool.core.object"))
        map["spool.core.Array"] = AstNode.TypeNode("spool.core.Array", true, TypeRef("spool.core.object"))
        map["spool.core.Func"] = AstNode.TypeNode("spool.core.Func", true, TypeRef("spool.core.object"))
        map["spool.core.Void"] = AstNode.TypeNode("spool.core.Void", true, TypeRef("spool.core.object"))
        map["spool.core.Console"] = consoleType()
        map["spool.core.Random"] = AstNode.TypeNode("spool.core.Random", true, TypeRef("spool.core.object"))

        map["spool.core.number.Number"] = AstNode.TypeNode("spool.core.number.Number", true, TypeRef("spool.core.object"))
        map["spool.core.number.Byte"] = AstNode.TypeNode("spool.core.number.Byte", true, TypeRef("spool.core.number.Number"))
        map["spool.core.number.UByte"] = AstNode.TypeNode("spool.core.number.UByte", true, TypeRef("spool.core.number.Number"))
        map["spool.core.number.Int16"] = AstNode.TypeNode("spool.core.number.Int16", true, TypeRef("spool.core.number.Number"))
        map["spool.core.number.UInt16"] = AstNode.TypeNode("spool.core.number.UInt16", true, TypeRef("spool.core.number.Number"))
        map["spool.core.number.Int32"] = AstNode.TypeNode("spool.core.number.Int32", true, TypeRef("spool.core.number.Number"))
        map["spool.core.number.UInt32"] = AstNode.TypeNode("spool.core.number.UInt32", true, TypeRef("spool.core.number.Number"))
        map["spool.core.number.Int64"] = AstNode.TypeNode("spool.core.number.Int64", true, TypeRef("spool.core.number.Number"))
        map["spool.core.number.UInt64"] = AstNode.TypeNode("spool.core.number.UInt64", true, TypeRef("spool.core.number.Number"))
        map["spool.core.number.Int128"] = AstNode.TypeNode("spool.core.number.Int128", true, TypeRef("spool.core.number.Number"))
        map["spool.core.number.UInt128"] = AstNode.TypeNode("spool.core.number.UInt128", true, TypeRef("spool.core.number.Number"))
        map["spool.core.number.Float32"] = AstNode.TypeNode("spool.core.number.Float32", true, TypeRef("spool.core.number.Number"))
        map["spool.core.number.Float64"] = AstNode.TypeNode("spool.core.number.Float64", true, TypeRef("spool.core.number.Number"))
    }

    operator fun set(canonicalName: String, node: AstNode) {
        when (node) {
            is AstNode.TypeNode, is AstNode.FunctionNode, is AstNode.VariableNode -> map[canonicalName] = node
        }
    }

    fun getTypeNode(canonicalName: String): AstNode.TypeNode {
        val node = map[canonicalName]
        if (node is AstNode.TypeNode) return node
        throw Exception()
    }

    fun allFunctions(): List<AstNode.FunctionNode> = map.values.filterIsInstance<AstNode.FunctionNode>()

    fun allClasses(): List<AstNode.TypeNode> = map.values.filterIsInstance<AstNode.TypeNode>()

    private fun consoleType(): AstNode.TypeNode {
        val printlnFunction = AstNode.FunctionNode(
            "println",
            listOf(
                "self" to TypeRef("spool.core.Console"),
                "toPrint" to TypeRef("spool.core.Object")
            ),
            listOf(),
            true
        )

        val constructor = AstNode.ConstructorNode(listOf(), listOf())

        return AstNode.TypeNode("spool.core.Console", true, TypeRef("spool.core.Object"), listOf(), listOf(constructor), listOf(printlnFunction))
    }
}