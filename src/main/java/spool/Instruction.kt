package spool

class Instruction (private val type: InstructionType, private val data1: Any?, private val data2: Any?) {
    fun toBytes(byteList: MutableList<UByte>) {
        byteList.add(type.byte)
        encodeData(data1, byteList)
        encodeData(data2, byteList)
    }

    fun encodeData(data: Any?, byteList: MutableList<UByte>) {
        if (data is Boolean) {
            if (data == true) byteList.add(1u)
            else byteList.add(0u)
        }
        else if (data is UShort) {
            val first = data.toUByte()
            val second = (data.toShort().toInt() shl 8).toShort().toUByte()
            byteList.add(first)
            byteList.add(second)
        }
    }
}

class Chunk(val bytes: List<UByte>, val constants: List<Any>, val names: List<String>) {
    override fun toString(): String {
        val string = ""

        return string
    }
}

enum class InstructionType(val byte: UByte) {
    GET_TRUE(0u),
    GET_FALSE(1u),
    GET(2u),
    SET(3u),
    DECLARE(4u)
}