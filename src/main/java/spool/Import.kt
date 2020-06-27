package spool

class Import(import: String) {
    private val split: List<String> = import.split(".")

    fun endsWith(string: String): Boolean {
        val s = string.split(".").reversed()

        if (s.size > split.size) return false

        for (pair in split.reversed().subList(0, s.size).withIndex().map { Pair(it.value, s[it.index]) }) {
            if (pair.first != pair.second) return false
        }

        return true
    }

    fun getName(): String {
        return split.joinToString(".")
    }

    companion object {
        fun defaultImports(): List<Import> {
            val prefix = "spool.core."
            val prefix2 = prefix + "number."

            return listOf(
                Import(prefix + "Object"),
                Import(prefix + "Boolean"),
                Import(prefix + "Char"),
                Import(prefix + "String"),
                Import(prefix + "Array"),
                Import(prefix + "Func"),
                Import(prefix + "Void"),
                Import(prefix + "Console"),
                Import(prefix + "Random"),

                Import(prefix2 + "Number"),
                Import(prefix2 + "Byte"),
                Import(prefix2 + "UByte"),
                Import(prefix2 + "Int16"),
                Import(prefix2 + "UInt16"),
                Import(prefix2 + "Int32"),
                Import(prefix2 + "UInt32"),
                Import(prefix2 + "Int64"),
                Import(prefix2 + "UInt64"),
                Import(prefix2 + "Int128"),
                Import(prefix2 + "UInt128"),
                Import(prefix2 + "Float32"),
                Import(prefix2 + "Float64")
            )
        }
    }
}