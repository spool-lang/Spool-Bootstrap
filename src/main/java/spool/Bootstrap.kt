package spool

import blue.endless.jankson.Jankson
import blue.endless.jankson.JsonArray
import blue.endless.jankson.JsonPrimitive
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.Charset
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.Exception
import kotlin.system.exitProcess

@ExperimentalUnsignedTypes
fun main(args: Array<String>) {
    if (args.size != 1) {
        println("Please specify the project file.")
        exitProcess(-1)
    }

    val projectDirectory = File(args[0])
    val projectFile = projectDirectory.resolve("project.json5")
    val loadedProject = loadProject(projectFile)

    val db = FileDB()

    val parsedCode = loadedProject.sources.map { lexAndParse(it, db) }
    val resolver = TypeResolver(db)
    parsedCode.forEach { resolver.resolve(it.ast) }

    val printer = AstPrinter()
    parsedCode.forEach {
        val json = printer.printAst(it.ast)
        projectDirectory.resolve("ast/${it.path}.json5").apply { parentFile.mkdirs() }.writeText(json)
    }

    val bytecodeGen = BytecodeGenerator()
    val compiledFiles = parsedCode.map { loadedAst ->
        val bytes = mutableListOf<UByte>()
        val bytecode = bytecodeGen.run(loadedAst.ast)
        bytecode.forEach(Bytecode::print)
        bytecode.forEach { it.addBytes(bytes) }
        CompiledFile(bytes, loadedAst.path)
    }

    val output = projectDirectory.resolve("out\\${loadedProject.name}.zip").apply { parentFile.mkdirs() }
    val outputStream = ZipOutputStream(FileOutputStream(output))

    compiledFiles.forEach { compiled ->
        val e = ZipEntry("${compiled.path}.txt")
        outputStream.putNextEntry(e)
        val data = compiled.bytes.map(UByte::toByte).toByteArray()
        outputStream.write(data, 0, data.size)
        outputStream.closeEntry()
    }

    outputStream.close()
}

fun loadProject(projectFile: File): LoadedProject {
    val projectConfig = Jankson.builder().build().load(projectFile)

    val projectNameJson = projectConfig["name"]
    val sourcesJson = projectConfig["sources"]

    if (projectNameJson !is JsonPrimitive || sourcesJson !is JsonArray) throw Exception()


    val name = projectNameJson.asString()
    val sources = sourcesJson.filterIsInstance<JsonPrimitive>().map { it.asString() }.map {
        val file = projectFile.parentFile.resolve("src/${it}.spool")
        return@map SourceFile(file.readText(), it)
    }

    return LoadedProject(name, sources)
}

data class LoadedProject(val name: String, val sources: List<SourceFile>)

data class SourceFile(val contents: String, val path: String)

fun lexAndParse(source: SourceFile, db: FileDB): LoadedAst {
    val lexer = Lexer(source.contents)
    val tokens = lexer.lex()
    val parser = Parser(tokens)
    return LoadedAst(parser.parse(db), source.path)
}

data class LoadedAst(val ast: AstNode, val path: String)

@ExperimentalUnsignedTypes
data class CompiledFile(val bytes: List<UByte>, val path: String)