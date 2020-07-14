package cafe.adriel.satchel.util

import java.io.Serializable

data class SampleDataClass(
    val string: String = "",
    val boolean: Boolean = false,
    val int: Int = 0,
    val long: Long = 0L,
    val float: Float = .0F,
    val double: Double = .0
) : Serializable
