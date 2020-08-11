package cafe.adriel.satchel.serializer.protobuf.lite

import cafe.adriel.satchel.serializer.SatchelSerializer
import cafe.adriel.satchel.serializer.protobuf.lite.proto.SatchelProto

object ProtobufLiteSatchelSerializer : SatchelSerializer {

    override suspend fun serialize(data: Map<String, Any>): ByteArray =
        SatchelProto.Satchel
            .newBuilder()
            .putAllStorage(data.mapValues { it.value.toProtoValue() })
            .build()
            .toByteArray()

    override fun deserialize(data: ByteArray): Map<String, Any> =
        when {
            data.isEmpty() -> emptyMap()
            else ->
                SatchelProto.Satchel
                    .parseFrom(data)
                    .storageMap
                    .mapValues { it.value.toAnyValue() }
        }
}
