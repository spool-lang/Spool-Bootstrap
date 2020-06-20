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
        val constants = mutableListOf<Any>()
        val instructions = mutableListOf<Instruction>()

        fun addInstruction(instruction: Instruction) {
            instructions.add(instruction)
        }

        fun addConstant(any: Any) {
            if (!constants.contains(any)) constants.add(any)
        }

        override fun addBytes(bytes: MutableList<UByte>) {
            var string = if (name == "main") {"#main("} else {"#func($name;"}
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

            for (constant in constants) {
                string = if (!started) {"$string$constant"} else {"$string,$constant"}
                started = true
            }

            var instructionBytes = mutableListOf<UByte>()
            instructions.forEach { it.toBytes(instructionBytes) }
            string = "$string;${instructionBytes.size})"
            var stringBytes = string.toByteArray().toUByteArray().toMutableList()
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
            println("instructions:")
            instructions.forEach { println(it) }
        }
    }
    class Clazz(val name: String, val superClass: String, val fields: List<AstNode.VariableNode>, val functions: List<spool.Chunk>): Bytecode() {
        override fun addBytes(bytes: MutableList<UByte>) {
            val headerBytes = "#class($name;$superClass)".toByteArray().toUByteArray().toMutableList()
            bytes.addAll(headerBytes)

            for (field in fields) {
                val fieldString = "#field(${field.const};${field.name};${field.type}"
            }

            for (function in functions) {
                function.addBytes(bytes)
            }

            val endBytes = "#endclass".toByteArray().toUByteArray().toMutableList()
            bytes.addAll(endBytes)
        }

        override fun print() {
            println("class")
            println("name: $name")
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
        }
        else if (data is UShort) {
            val first = data.toUByte()
            val second = (data.toShort().toInt() shr 8).toShort().toUByte()
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
    LOGIC_NEGATE(26u),
    EXIT_BLOCK(28u),
    CALL_INSTANCE(30u),
    GET_TYPE(32u)
}