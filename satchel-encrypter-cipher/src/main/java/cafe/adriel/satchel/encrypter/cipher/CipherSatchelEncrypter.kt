package cafe.adriel.satchel.encrypter.cipher

import cafe.adriel.satchel.encrypter.SatchelEncrypter
import cafe.adriel.satchel.encrypter.cipher.CipherSatchelEncrypter.CipherKey.IdentityCertificate
import cafe.adriel.satchel.encrypter.cipher.CipherSatchelEncrypter.CipherKey.SecretKey
import java.security.Key
import java.security.cert.Certificate
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.Cipher

class CipherSatchelEncrypter private constructor(
    private val cipher: Cipher,
    private val cipherKey: CipherKey
) : SatchelEncrypter {

    companion object {

        private const val DEFAULT_TRANSFORMATION = "AES"

        fun with(cipher: Cipher, cipherKey: CipherKey): CipherSatchelEncrypter =
            CipherSatchelEncrypter(cipher, cipherKey)

        fun with(cipherKey: CipherKey, transformation: String = DEFAULT_TRANSFORMATION): CipherSatchelEncrypter =
            with(Cipher.getInstance(transformation), cipherKey)
    }

    sealed class CipherKey {
        data class SecretKey(val key: Key, val params: AlgorithmParameterSpec? = null) : CipherKey()
        data class IdentityCertificate(val certificate: Certificate) : CipherKey()
    }

    override suspend fun encrypt(data: ByteArray): ByteArray =
        cipher.run {
            initCipher(Cipher.ENCRYPT_MODE)
            doFinal(data)
        }

    override fun decrypt(data: ByteArray): ByteArray =
        when {
            data.isEmpty() -> data
            else -> cipher.run {
                initCipher(Cipher.DECRYPT_MODE)
                doFinal(data)
            }
        }

    private fun initCipher(mode: Int) {
        when (cipherKey) {
            is SecretKey -> cipher.init(mode, cipherKey.key, cipherKey.params)
            is IdentityCertificate -> cipher.init(mode, cipherKey.certificate)
        }
    }
}
