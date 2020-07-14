package cafe.adriel.satchel.serializer

interface SatchelSerializer {

    suspend fun serialize(data: Map<String, Any>): ByteArray

    fun deserialize(data: ByteArray): Map<String, Any>
}
