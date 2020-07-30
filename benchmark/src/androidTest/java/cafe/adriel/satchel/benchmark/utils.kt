package cafe.adriel.satchel.benchmark

import cafe.adriel.satchel.ktx.serialize
import java.io.File
import java.util.UUID

val sampleData = mapOf(
    *Array(1_000) { "Key $it" to "Value $it" }
)

val serializedSampleData = sampleData.serialize()

val randomName: String
    get() = UUID.randomUUID().toString()

val randomFile: File
    get() = File.createTempFile(randomName, "")
