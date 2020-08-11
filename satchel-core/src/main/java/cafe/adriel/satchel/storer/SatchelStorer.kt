package cafe.adriel.satchel.storer

interface SatchelStorer {

    suspend fun store(data: ByteArray)

    fun retrieve(): ByteArray
}
