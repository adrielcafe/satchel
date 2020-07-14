package cafe.adriel.satchel.serializer.flatbuffers

import cafe.adriel.satchel.serializer.SatchelSerializer
import com.google.flatbuffers.ArrayReadWriteBuf
import com.google.flatbuffers.FlexBuffers
import com.google.flatbuffers.FlexBuffersBuilder

object FlatbuffersSatchelSerializer : SatchelSerializer {

    override suspend fun serialize(data: Map<String, Any>): ByteArray =
        FlexBuffersBuilder().run {
            putMap(data)
            finish().let { byteBuffer ->
                byteBuffer.array().sliceArray(0..byteBuffer.limit())
            }
        }

    override fun deserialize(data: ByteArray): Map<String, Any> =
        FlexBuffers.getRoot(ArrayReadWriteBuf(data, data.lastIndex))
            .asMap()
            .toStorage()
}
