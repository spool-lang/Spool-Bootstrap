package spool

import java.io.File
import kotlin.Exception
import kotlin.system.exitProcess

val test = ""

fun main(args: Array<String>) {

    if (args.size != 1) {
        println("Please specify the project file.")
        exitProcess(-1)
    }

    val test = File(args[0]).readText()
    println(test)

    val lexer = Lexer(test)
    val tokens: List<Token>

    try {
        tokens = lexer.lex()
    } catch (e: Exception) {
        e.printStackTrace()
        exitProcess(-2)
    }

    tokens.forEach { println(it) }

    val parser = Parser(tokens)
    val fileDB: FileDB

    try {
        fileDB = parser.parse()
    } catch (e: Exception) {
        e.printStackTrace()
        exitProcess(-2)
    }

    fileDB.map["main"]?.let {
        val json = AstPrinter().printAst(it)
        File("ast.json").writeText(json)
        val chunk = BytecodeGenerator().run(it)
        chunk.print()
        val bytes = chunk.toBytes()

        File("test.sbc").writeBytes(bytes.toTypedArray().toUByteArray().toByteArray())
    }
}