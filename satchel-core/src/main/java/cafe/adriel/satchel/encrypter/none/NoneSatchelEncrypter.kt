package cafe.adriel.satchel.encrypter.none

import cafe.adriel.satchel.encrypter.SatchelEncrypter

object NoneSatchelEncrypter : SatchelEncrypter {

    override suspend fun encrypt(data: ByteArray): ByteArray =
        data

    override fun decrypt(data: ByteArray): ByteArray =
        data
}
