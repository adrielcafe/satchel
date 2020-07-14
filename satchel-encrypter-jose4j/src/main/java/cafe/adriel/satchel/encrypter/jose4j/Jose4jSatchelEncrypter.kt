package cafe.adriel.satchel.encrypter.jose4j

import cafe.adriel.satchel.encrypter.SatchelEncrypter
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers
import org.jose4j.jwe.JsonWebEncryption
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers
import org.jose4j.jwk.PublicJsonWebKey

class Jose4jSatchelEncrypter private constructor(
    private val jwe: JsonWebEncryption,
    private val jwk: PublicJsonWebKey
) : SatchelEncrypter {

    companion object {

        private const val DEFAULT_HEADER_ALGORITHM = KeyManagementAlgorithmIdentifiers.RSA_OAEP_256
        private const val DEFAULT_CONTENT_ALGORITHM = ContentEncryptionAlgorithmIdentifiers.AES_256_GCM

        fun with(jwe: JsonWebEncryption, jwk: PublicJsonWebKey): Jose4jSatchelEncrypter =
            Jose4jSatchelEncrypter(jwe, jwk)

        fun with(jwk: PublicJsonWebKey): Jose4jSatchelEncrypter =
            with(
                jwe = JsonWebEncryption().apply {
                    algorithmHeaderValue = DEFAULT_HEADER_ALGORITHM
                    encryptionMethodHeaderParameter = DEFAULT_CONTENT_ALGORITHM
                },
                jwk = jwk
            )
    }

    override suspend fun encrypt(data: ByteArray): ByteArray =
        jwe.run {
            key = jwk.key
            setPlaintext(data)
            compactSerialization.toByteArray()
        }

    @OptIn(ExperimentalStdlibApi::class)
    override fun decrypt(data: ByteArray): ByteArray =
        when {
            data.isEmpty() -> data
            else -> jwe.run {
                key = jwk.privateKey
                compactSerialization = data.decodeToString()
                plaintextBytes
            }
        }
}
