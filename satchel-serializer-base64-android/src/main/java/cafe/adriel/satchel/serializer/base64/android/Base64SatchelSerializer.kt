package cafe.adriel.satchel.serializer.base64.android

import android.util.Base64
import cafe.adriel.satchel.core.ktx.deserialize
import cafe.adriel.satchel.core.ktx.serialize
import cafe.adriel.satchel.serializer.SatchelSerializer

object Base64SatchelSerializer : SatchelSerializer {

    private const val FLAGS = Base64.NO_PADDING or Base64.NO_WRAP

    override suspend fun serialize(data: Map<String, Any>): ByteArray =
        Base64.encode(data.serialize(), FLAGS)

    override fun deserialize(data: ByteArray): Map<String, Any> =
        Base64.decode(data, FLAGS).deserialize()
}
