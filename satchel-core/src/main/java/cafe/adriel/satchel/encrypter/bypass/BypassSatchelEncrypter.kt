package cafe.adriel.satchel.encrypter.bypass

import cafe.adriel.satchel.encrypter.SatchelEncrypter

object BypassSatchelEncrypter : SatchelEncrypter {

    override suspend fun encrypt(data: ByteArray): ByteArray =
        data

    override fun decrypt(data: ByteArray): ByteArray =
        data
}
