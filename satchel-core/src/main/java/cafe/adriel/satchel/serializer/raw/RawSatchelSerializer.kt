package cafe.adriel.satchel.serializer.raw

import cafe.adriel.satchel.ktx.deserialize
import cafe.adriel.satchel.ktx.serialize
import cafe.adriel.satchel.serializer.SatchelSerializer

object RawSatchelSerializer : SatchelSerializer {

    override suspend fun serialize(data: Map<String, Any>): ByteArray =
        data.serialize()

    override fun deserialize(data: ByteArray): Map<String, Any> =
        when {
            data.isEmpty() -> emptyMap()
            else -> data.deserialize()
        }
}
