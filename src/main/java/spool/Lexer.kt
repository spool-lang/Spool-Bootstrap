package spool

class Lexer(private val source: String) {
    private var tokens: MutableList<Token> = mutableListOf()
    private var index: Int = 0
    private var row: Long = 0
    private var column: Long = 0
    private val keywords: MutableMap<String, TokenType> = mutableMapOf()

    @Throws(Exception::class)
    fun lex(): List<Token> {

        var char = ' '
        while (char != '\u0000') {
            consumeWhitespace()
            char = next()
            when (char) {
                // Operators
                '+' -> pattern(TokenType.PLUS_ASSIGN, "+=") || symbol(TokenType.PLUS, '+')
                '-' -> pattern(TokenType.MINUS_ASSIGN, "-=") || pattern(TokenType.R_ARROW, "->") || symbol(TokenType.MINUS, '-')
                '*' -> pattern(TokenType.MULTIPLY_ASSIGN, "*=") || symbol(TokenType.MULTIPLY, '*')
                '/' -> pattern(TokenType.DIVIDE_ASSIGN, "/=") || symbol(TokenType.DIVIDE, '/')
                '^' -> pattern(TokenType.POW_ASSIGN, "^=") || symbol(TokenType.POW, '^')
                '=' -> pattern(TokenType.EQUAL, "==") || symbol(TokenType.ASSIGN, '=')
                '.' -> symbol(TokenType.DOT, '.')

                // Other
                ',' -> symbol(TokenType.COMMA, ',')
                ':' -> symbol(TokenType.COLIN, ':')
                '"' -> string()

                // Logic Operators
                '<' -> pattern(TokenType.LESS_EQUAL, "<=") || symbol(TokenType.LESS, '<')
                '>' -> pattern(TokenType.GREATER_EQUAL, ">=") || symbol(TokenType.GREATER, ">")
                '!' -> pattern(TokenType.NOT_EQUAL, "!=") || symbol(TokenType.NOT, '!')

                // Brackets & Braces
                '[' -> symbol(TokenType.SQUARE_LEFT, '[')
                ']' -> symbol(TokenType.SQUARE_RIGHT, ']')
                '{' -> symbol(TokenType.BRACE_LEFT, '{')
                '}' -> symbol(TokenType.BRACE_RIGHT, '}')
                '(' -> symbol(TokenType.PAREN_LEFT, '(')
                ')' -> symbol(TokenType.PAREN_RIGHT, ')')

                // Anything that can't be parsed with pattern matching.
                else -> {
                    when {
                        char.isLetter() -> identifier(char)
                        char.isDigit() -> number(char)
                    }
                }
            }
        }
        tokens.add(Token(TokenType.EOF, row, column, null, null))
        val lexedTokens = tokens
        tokens = arrayListOf()
        return lexedTokens
    }

    private fun consumeWhitespace() {
        outer@
        while (peek(0).isWhitespace()) {
            when (next()) {
                ' ', '\r', '\t' -> {}
                '\n' -> {
                    row++
                    column = 0
                }
                else -> break@outer
            }
        }
    }

    private fun pattern(type: TokenType, pattern: String): Boolean {
        val length = pattern.length
        for (i in 1 until length) {
            if (peek(i - 1) != pattern[i]) {
                return false
            }
        }
        index += length - 1
        symbol(type, pattern)
        return true
    }

    private fun  symbol(type: TokenType, lexeme: Char): Boolean = symbol(type, "$lexeme")

    private fun symbol(type: TokenType, lexeme: String): Boolean {
        tokens.add(Token(type, row, column, lexeme, null))
        return true
    }

    private fun identifier(first: Char) {
        var word = "$first"

        while (peek(0).isLetter() || peek(0).isDigit()) word = "$word${next()}"

        var type = keywords[word]
        if (type == null) type = TokenType.ID
        when (type) {
            TokenType.TRUE -> tokens.add(Token(type, row, column, null, true))
            TokenType.FALSE -> tokens.add(Token(type, row, column, null, false))
            else -> tokens.add(Token(type, row, column, word, null))
        }
    }

    private fun number(first: Char) {
        var number = "$first"
        while (peek(0).isDigit()) number = "$number${next()}"

        tokens.add(Token(TokenType.NUMBER, row, column, null, number.toInt()))
    }

    private fun string() {
        var string = "\""
        while (peek(0) != '"') string ="$string${next()}"
        string = "$string\""
        next()

        tokens.add(Token(TokenType.STRING, row, column, null, string))
    }

    private fun next(): Char = if (index < source.length) { source[index++] } else { '\u0000'}

    private fun peek(count: Int): Char = if (index + count < source.length) { source[index + count] } else { '\u0000' }

    init {
        keywords["func"] = TokenType.FUNC
        keywords["class"] = TokenType.CLASS
        keywords["new"] = TokenType.NEW
        keywords["constructor"] = TokenType.CONSTRUCTOR
        keywords["var"] = TokenType.VAR
        keywords["const"] = TokenType.CONST
        keywords["namespace"] = TokenType.NAMESPACE
        keywords["use"] = TokenType.USE
        keywords["native"] = TokenType.NATIVE
        keywords["if"] = TokenType.IF
        keywords["else"] = TokenType.ELSE
        keywords["loop"] = TokenType.LOOP
        keywords["next"] = TokenType.NEXT
        keywords["break"] = TokenType.BREAK
        keywords["return"] = TokenType.RETURN
        keywords["main"] = TokenType.MAIN
        keywords["and"] = TokenType.AND
        keywords["or"] = TokenType.OR
        keywords["true"] = TokenType.TRUE
        keywords["false"] = TokenType.FALSE
    }
}