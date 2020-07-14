package cafe.adriel.satchel.util

import java.io.Serializable

sealed class SampleSealedClass : Serializable {

    data class DataClass(
        val string: String = "",
        val boolean: Boolean = false,
        val int: Int = 0,
        val long: Long = 0L,
        val float: Float = .0F,
        val double: Double = .0,
        val dataClass: SampleDataClass = SampleDataClass()
    ) : SampleSealedClass()
}
