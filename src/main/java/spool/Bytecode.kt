package spool

@ExperimentalUnsignedTypes
typealias Chunk = Bytecode.Chunk
@ExperimentalUnsignedTypes
typealias Clazz = Bytecode.Clazz

@ExperimentalUnsignedTypes
sealed class Bytecode {
    abstract fun addBytes(bytes: MutableList<UByte>)
    abstract fun print();

    class Chunk: Bytecode() {
        var name = ""
        val params = mutableListOf<String>()
        val names = mutableListOf<String>()
        val jumps = mutableListOf<JumpPoint>()
        val constants = mutableListOf<Any>()
        val instructions = mutableListOf<Instruction>()

        fun addInstruction(instruction: Instruction) {
            instructions.add(instruction)
        }

        fun addConstant(any: Any) {
            if (!constants.contains(any)) constants.add(any)
        }

        fun addName(name: String) {
            if (!names.contains(name)) names.add(name)
        }

        fun addJump(point: JumpPoint) {
            jumps.add(point)
        }

        override fun addBytes(bytes: MutableList<UByte>) {
            var string = when (name) {
                "main" -> { "#main(" }
                "constructor" -> { "#ctor(" }
                else -> {"#func($name;"}
            }
            var started = false

            for (param in params) {
                string = if (!started) {"$string$param"} else {"$string,$param"}
                started = true
            }

            started = false

            if (name != "main") string = "$string;"
            for (name in names) {
                string = if (!started) {"$string$name"} else {"$string,$name"}
                started = true
            }

            started = false
            string = "$string;"
            for (jump in jumps) {
                string = if (!started) { "$string${jump.index}" } else  { "$string,${jump.index}" }
                started = true
            }

            started = false
            string = "$string;"

            for (constant in constants) {
                string = if (!started) {"$string$constant"} else {"$string,$constant"}
                started = true
            }

            val instructionBytes = mutableListOf<UByte>()
            instructions.forEach { it.toBytes(instructionBytes) }
            println(instructionBytes)
            string = "$string;${instructionBytes.size})"
            val stringBytes = string.toByteArray().toUByteArray().toMutableList()
            bytes.addAll(stringBytes)
            bytes.addAll(instructionBytes)
        }

        override fun print() {
            println("function")
            println("name: $name")
            println("params: $params")
            println("constants:")
            constants.forEach { println(it) }
            println("names:")
            names.forEach { println(it) }
            println("jump points:")
            jumps.forEach { println(it) }
            println("instructions:")
            instructions.withIndex().forEach {
                println("${it.index}: ${it.value}")
            }
            println()
        }
    }
    class Clazz(val name: String, val superClass: String, val fields: List<AstNode.VariableNode>, val constructors: List<spool.Chunk>, val functions: List<spool.Chunk>): Bytecode() {
        override fun addBytes(bytes: MutableList<UByte>) {
            val headerBytes = "#class($name;$superClass)".toByteArray().toUByteArray().toMutableList()
            bytes.addAll(headerBytes)

            for (field in fields) {
                val fieldString = "#prop(${if (field.const) 0 else 1};${field.name};${field.type.name})"
                bytes.addAll(fieldString.toByteArray().toUByteArray().toMutableList())
            }

            for (constructor in constructors) {
                constructor.addBytes(bytes)
            }

            for (function in functions) {
                function.addBytes(bytes)
            }

            val endBytes = "#endclass".toByteArray().toUByteArray().toMutableList()
            bytes.addAll(endBytes)
        }

        override fun print() {
            println("class: $name")
            println("supertype: $superClass")
            println("Constructors:")
            for (constructor in constructors) {
                constructor.print()
                println()
            }
            println("Functions: ")
            for (function in functions) {
                function.print()
                println()
            }
        }
    }
}

@ExperimentalUnsignedTypes
data class Instruction(private val type: InstructionType, private val data1: Any? = null, private val data2: Any? = null) {
    fun toBytes(byteList: MutableList<UByte>) {
        byteList.add(type.byte)
        encodeData(data1, byteList)
        encodeData(data2, byteList)
    }

    private fun encodeData(data: Any?, byteList: MutableList<UByte>) {
        if (data is Boolean) {
            if (data == true) byteList.add(1u)
            else byteList.add(0u)
        } else if (data is UShort) {
            val first = data.toUByte()
            val second = (data.toShort().toInt() shr 8).toShort().toUByte()
            byteList.add(first)
            byteList.add(second)
        } else if (data is JumpPoint) {
            val index = data.index ?: throw Exception()
            val first = index.toUByte()
            val second = (index.toShort().toInt() shr 8).toShort().toUByte()
            byteList.add(first)
            byteList.add(second)
        }
    }
}

@ExperimentalUnsignedTypes
enum class InstructionType(val byte: UByte) {
    GET_TRUE(0u),
    GET_FALSE(1u),
    DECLARE(2u),
    SET(3u),
    GET(4u),
    NEW(5u),
    INSTANCE_GET(6u),
    INSTANCE_SET(7u),
    INIT_ARRAY(8u),
    INDEX_GET(9u),
    INDEX_SET(9u),
    ADD(11u),
    SUBTRACT(12u),
    MULTIPLY(13u),
    DIVIDE(14u),
    POWER(15u),
    NUM_NEGATE(16u),
    LESS(17u),
    GREATER(18u),
    EQ(19u),
    LESS_EQ(20u),
    GREATER_EQ(21u),
    NOT_EQ(22u),
    AND(23u),
    OR(24u),
    IS(25u),
    LOGIC_NEGATE(26u),
    JUMP(27u),
    EXIT_BLOCK(28u),
    CALL(29u),
    CALL_INSTANCE(30u),
    RETURN(31u),
    GET_TYPE(32u)
}