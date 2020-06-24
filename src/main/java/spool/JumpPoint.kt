package spool

@ExperimentalUnsignedTypes
class JumpPoint(var index: UShort? = null) {

    override fun toString(): String {
        return "$index"
    }
}