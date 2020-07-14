package cafe.adriel.satchel.encrypter

interface SatchelEncrypter {

    suspend fun encrypt(data: ByteArray): ByteArray

    fun decrypt(data: ByteArray): ByteArray
}
