package cafe.adriel.satchel.util

import java.io.Serializable

class SampleClass(
    val string: String = "",
    val boolean: Boolean = false,
    val int: Int = 0,
    val long: Long = 0L,
    val float: Float = .0F,
    val double: Double = .0
) : Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SampleClass

        if (string != other.string) return false
        if (boolean != other.boolean) return false
        if (int != other.int) return false
        if (long != other.long) return false
        if (float != other.float) return false
        if (double != other.double) return false

        return true
    }

    override fun hashCode(): Int {
        var result = string.hashCode()
        result = 31 * result + boolean.hashCode()
        result = 31 * result + int
        result = 31 * result + long.hashCode()
        result = 31 * result + float.hashCode()
        result = 31 * result + double.hashCode()
        return result
    }
}
