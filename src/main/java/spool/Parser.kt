package spool

class Parser(private val tokens: List<Token>) {
    private val imports: MutableList<Import> = mutableListOf()
    private var current: Int = 0
    private var namespace: String = ""

    fun parse(fileDB: FileDB): AstNode.FileNode {
        resolveNamespaceImports()
        val statements: MutableMap<String, AstNode> = mutableMapOf()

        while (!isAtEnd()) {
            val node = topLevelDeclaration()
            if (node != null) {
                when (node) {
                    is AstNode.TypeNode -> {
                        fileDB[node.name] = node
                        statements[node.name] = node
                    }
                    is AstNode.VariableNode -> {
                        fileDB[node.name] = node
                        statements[node.name] = node
                    }
                    is AstNode.FunctionNode -> {
                        fileDB[node.name] = node
                        statements[node.name] = node
                    }
                }
            }
        }

        return AstNode.FileNode(statements, namespace, imports)
    }

    private fun resolveNamespaceImports() {
        consume(TokenType.NAMESPACE, "Expected namespace declaration.")
        namespace = ""

        while (match(TokenType.ID)) {
            namespace = "$namespace${previous().lexeme}"
            if (match(TokenType.DOT)) namespace = "$namespace."
            else break
        }

        var importName = ""
        while (match(TokenType.USE)) {
            while (match(TokenType.ID)) {
                importName = "$importName${previous().lexeme}"
                if (match(TokenType.DOT)) importName = "$importName."
                else break
            }
            imports.add(Import(importName))
            importName = ""
        }
    }

    private fun topLevelDeclaration(): AstNode? {
        try {
            if (match(TokenType.MAIN)) return mainFunction()
            if (match(TokenType.CLASS)) return clazz()
            // if (match(TokenType.VAR));
            // if (match(TokenType.CONST));
            // if (match(TokenType.FUNC));
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun declaration(): AstNode? {
        try {
            if (match(TokenType.VAR)) return variable(false);
            if (match(TokenType.CONST)) return variable(true);

            return expression()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun mainFunction(): AstNode.FunctionNode {
        consume(TokenType.BRACE_LEFT, "Expected '{' before main body.")
        val body = body()
        return AstNode.FunctionNode("main", body, listOf())
    }

    private fun function(instanceType: TypeRef? = null): AstNode.FunctionNode {
        val name = consume(TokenType.ID, "Expected function name.").lexeme!!
        val params = mutableListOf<Pair<String, TypeRef>>()

        if (instanceType != null) {
            params.add(Pair("self", instanceType))
        }

        consume(TokenType.PAREN_LEFT, "Expected parens before and after function params.")

        while (check(TokenType.ID)) {
            val paramName = consume(TokenType.ID, "Expected parameter name.").lexeme!!

            consume(TokenType.COLIN, "Expected colin after parameter name.")
            val typeName = typeName()

            params.add(Pair(paramName, TypeRef(typeName, null)))
        }

        consume(TokenType.PAREN_RIGHT, "Expected parens before and after function params.")
        consume(TokenType.BRACE_LEFT, "Expected braces before function body.")
        val body = body()
        return AstNode.FunctionNode(name, body, params, instanceType != null)
    }

    private fun clazz(): AstNode.TypeNode? {
        val name = consume(TokenType.ID, "Expected class name.")
        val properties = mutableListOf<AstNode.VariableNode>()
        val constructors = mutableListOf<AstNode.ConstructorNode>()
        val functions = mutableListOf<AstNode.FunctionNode>()
        val type = TypeRef(name.lexeme!!)

        val supertype = if (match(TokenType.COLIN)) {
            TypeRef(consume(TokenType.ID, "Expected superclass name!").lexeme!!)
        } else {
            TypeRef("spool.core.Object")
        }

        consume(TokenType.BRACE_LEFT, "Expected class body.")

        while (!check(TokenType.BRACE_RIGHT)) {
            when {
                match(TokenType.VAR) -> properties.add(variable(false))
                match(TokenType.CONST) -> properties.add(variable(true))
                match(TokenType.CONSTRUCTOR) -> constructors.add(constructor())
                match(TokenType.FUNC) -> functions.add(function(type))
            }
        }

        consume(TokenType.BRACE_RIGHT, "Expected end of class body.")

        imports.add(Import("$namespace.${name.lexeme}"))
        return AstNode.TypeNode("$namespace.${name.lexeme}", false, supertype, properties, constructors, functions)
    }

    private fun constructor(): AstNode.ConstructorNode {
        val params = mutableListOf<Pair<String, TypeRef>>()

        consume(TokenType.PAREN_LEFT, "Expected parens before and after function params.")

        while (check(TokenType.ID)) {
            val paramName = consume(TokenType.ID, "Expected parameter name.").lexeme!!

            consume(TokenType.COLIN, "Expected colin after parameter name.")
            val typeName = typeName()

            params.add(Pair(paramName, TypeRef(typeName, null)))
        }

        consume(TokenType.PAREN_RIGHT, "Expected parens before and after function params.")
        consume(TokenType.BRACE_LEFT, "Expected braces before function body.")
        val body = body()
        return AstNode.ConstructorNode(body, params)
    }

    private fun variable(constant: Boolean): AstNode.VariableNode {
        val name = consume(TokenType.ID, "Expected variable internalName.").lexeme!!

        consume(TokenType.COLIN, "Expected colin after variable name")
        val typeName = typeName()

        val initializer = if (match(TokenType.ASSIGN)) { expression() } else { null }

        return AstNode.VariableNode(name, TypeRef(typeName, null), constant, initializer)
    }

    private fun body(): List<AstNode> {
        val nodes = mutableListOf<AstNode>()

        while (!check(TokenType.BRACE_RIGHT) && !isAtEnd()) {
            declaration()?.let { nodes.add(it) }
        }

        consume(TokenType.BRACE_RIGHT, "Expected '}' after block.")
        return nodes
    }

    private fun expression(): AstNode {
        if (match(TokenType.BRACE_LEFT)) return AstNode.BlockNode(body())
        if (match(TokenType.IF)) return ifStatement()
        if (match(TokenType.LOOP)) return loop()
        if (match(TokenType.NEXT)) return AstNode.JumpNode(true)
        if (match(TokenType.BREAK)) return AstNode.JumpNode(false)
        if (match(TokenType.NEW)) return new()
        return assignment()
    }

    private fun ifStatement(): AstNode {
        consume(TokenType.PAREN_LEFT, "Expected '(' before if statement condition.")
        val condition = assignment()
        consume(TokenType.PAREN_RIGHT, "Expected '(' after if statement condition.")
        consume(TokenType.BRACE_LEFT, "...")
        val body = body()
        val then = if (match(TokenType.ELSE)) { expression() } else { null }

        return AstNode.IfNode(condition, body, then)
    }

    private fun loop(): AstNode {
        consume(TokenType.BRACE_LEFT, "Expected '{' before loop body!")
        val body = body()

        return AstNode.LoopNode(null, null, null, body)
    }

    private fun new(): AstNode {
        val typeName = typeName()
        consume(TokenType.PAREN_LEFT, "Expected '('")
        val arguments = getArguments()
        consume(TokenType.PAREN_RIGHT, "Expected ')'")
        return AstNode.ConstructorCallNode(TypeRef(typeName), arguments)
    }

    private fun assignment(): AstNode {
        val node = or()

        if (match(TokenType.ASSIGN)) {
            val equals = previous();
            val value = assignment();

            if (node is AstNode.IdNode) {
                val name = node.name
                return AstNode.AssignmentNode(name, value)
            }
            else if (node is AstNode.GetNode) {
                val name = node.name
                return AstNode.SetNode(name, node.source, value)
            }

            throw Exception("Invalid assignment target. [${equals.column}, ${equals.line}]")
        }

        return node
    }

    private fun or(): AstNode {
        var node = and()

        while (match(TokenType.OR)) {
            val operator = previous()
            val right = and()
            node = AstNode.BinaryNode(node, operator, right)
        }

        return node
    }

    private fun and(): AstNode {
        var node = equality()

        while (match(TokenType.AND)) {
            val operator = previous()
            val right = equality()
            node = AstNode.BinaryNode(node, operator, right)
        }

        return node
    }

    private fun equality(): AstNode {
        var node = comparison()

        while (match(TokenType.EQUAL, TokenType.NOT_EQUAL)) {
            val operator = previous()
            val right = comparison()
            node = AstNode.BinaryNode(node, operator, right)
        }

        return node
    }

    private fun comparison(): AstNode {
        var node = addition()

        while (match(TokenType.GREATER, TokenType.LESS, TokenType.GREATER_EQUAL, TokenType.LESS_EQUAL)) {
            val operator = previous()
            val right = addition()
            node = AstNode.BinaryNode(node, operator, right)
        }

        return node
    }

    private fun addition(): AstNode {
        var node = multiplication()

        while (match(TokenType.PLUS, TokenType.MINUS)) {
            val operator = previous()
            val right = multiplication()
            node = AstNode.BinaryNode(node, operator, right)
        }

        return node
    }

    private fun multiplication(): AstNode {
        var node = unary()

        while (match(TokenType.DIVIDE, TokenType.MULTIPLY)) {
            val operator = previous()
            val right = unary()
            node = AstNode.BinaryNode(node, operator, right)
        }

        return node
    }

    private fun unary(): AstNode {
        if (match(TokenType.NOT, TokenType.MINUS)) {
            val operator = previous()
            val right = unary()
            return AstNode.UnaryNode(right, operator)
        }

        return pow()
    }

    private fun pow(): AstNode {
        var node = call()

        while (match(TokenType.POW)) {
            val operator = previous()
            val right = call()
            node = AstNode.BinaryNode(node, operator, right)
        }

        return node
    }

    private fun call(): AstNode {
        var node = primary()

        while (true) {
            if (match(TokenType.PAREN_LEFT)) {
                val arguments = getArguments()
                consume(TokenType.PAREN_RIGHT, "Expected ')'")
                node = AstNode.FunctionCallNode(node, arguments)
            }
            else if (match(TokenType.DOT)) {
                val name = consume(TokenType.ID, "Expected property id.")
                node = AstNode.GetNode(name.lexeme!!, node)
            }
            else {
                break
            }
        }

        return node
    }

    private fun primary(): AstNode {
        return when {
            match(TokenType.STRING, TokenType.NUMBER) -> AstNode.LiteralNode(previous().literal!!)
            match(TokenType.TRUE) -> AstNode.LiteralNode(true)
            match(TokenType.FALSE) -> AstNode.LiteralNode(false)
            peek().type == TokenType.ID -> AstNode.IdNode(advance().lexeme!!)
            else -> throw Exception("Expected expression.")
        }

    }

    private fun getArguments(): List<AstNode> {
        val args = mutableListOf<AstNode>()

        if (!check(TokenType.PAREN_RIGHT)) {
            do {
                if (args.size >= 32) {
                    throw Exception("Cannot have more than 32 arguments")
                }
                args.add(expression())
            } while (match(TokenType.COMMA))
        }

        return args
    }

    private fun typeName(): String {
        var typeName = consume(TokenType.ID, "Expected variable type").lexeme!!
        while (peek().type == TokenType.DOT) {
            advance()
            typeName = "${typeName}.${consume(TokenType.ID, "Expected variable type").lexeme!!}"
        }
        return typeName
    }

    private fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }
        return false
    }

    private fun check(type: TokenType): Boolean = !isAtEnd() && peek().type == type

    private fun advance(): Token {
        if (!isAtEnd()) current += 1
        return previous()
    }

    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) return advance()

        throw Exception(message)
    }

    private fun isAtEnd() = peek().type == TokenType.EOF

    private fun peek() = tokens[current]

    private fun previous() = tokens[current - 1]
}