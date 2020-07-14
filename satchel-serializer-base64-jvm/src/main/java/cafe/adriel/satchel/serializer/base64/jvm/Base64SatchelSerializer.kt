package cafe.adriel.satchel.serializer.base64.jvm

import cafe.adriel.satchel.ktx.deserialize
import cafe.adriel.satchel.ktx.serialize
import cafe.adriel.satchel.serializer.SatchelSerializer
import java.util.Base64

object Base64SatchelSerializer : SatchelSerializer {

    override suspend fun serialize(data: Map<String, Any>): ByteArray =
        Base64.getEncoder().encode(data.serialize())

    override fun deserialize(data: ByteArray): Map<String, Any> =
        Base64.getDecoder().decode(data).deserialize()
}
