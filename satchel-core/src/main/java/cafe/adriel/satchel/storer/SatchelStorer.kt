package cafe.adriel.satchel.storer

interface SatchelStorer {

    suspend fun save(data: ByteArray)

    fun load(): ByteArray
}
