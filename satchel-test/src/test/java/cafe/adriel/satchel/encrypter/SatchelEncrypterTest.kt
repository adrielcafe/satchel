package cafe.adriel.satchel.encrypter

import cafe.adriel.satchel.ktx.serialize
import cafe.adriel.satchel.encrypter.cipher.CipherSatchelEncrypter
import cafe.adriel.satchel.encrypter.cipher.CipherSatchelEncrypter.CipherKey
import cafe.adriel.satchel.encrypter.jose4j.Jose4jSatchelEncrypter
import cafe.adriel.satchel.encrypter.none.NoneSatchelEncrypter
import cafe.adriel.satchel.encrypter.tink.jvm.TinkSatchelEncrypter
import cafe.adriel.satchel.util.SampleData
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.aead.AesGcmKeyManager
import java.util.stream.Stream
import javax.crypto.KeyGenerator
import kotlinx.coroutines.test.runBlockingTest
import org.jose4j.jwk.RsaJwkGenerator
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import strikt.api.expectThat
import strikt.assertions.contentEquals
import strikt.assertions.propertiesAreEqualTo

class SatchelEncrypterTest {

    init {
        AeadConfig.register()
    }

    private val noneEncrypter = NoneSatchelEncrypter
    private val cipherEncrypter = CipherSatchelEncrypter.with(
        cipherKey = CipherKey.SecretKey(
            key = KeyGenerator.getInstance("AES").apply { init(128) }.generateKey()
        )
    )
    private val tinkEncrypter = TinkSatchelEncrypter.with(
        keysetHandle = KeysetHandle.generateNew(AesGcmKeyManager.aes128GcmTemplate())
    )
    private val jose4jEncrypter = Jose4jSatchelEncrypter.with(
        jwk = RsaJwkGenerator.generateJwk(2048)
    )

    private val sampleByteArray = SampleData.allSupportedTypes.serialize()

    @TestFactory
    fun `when encrypt a byte array then decrypt correctly`(): Stream<DynamicTest> =
        Stream.of(
            noneEncrypter,
            cipherEncrypter,
            tinkEncrypter,
            jose4jEncrypter
        ).map { encrypter ->
            dynamicTest(encrypter::class.simpleName) {
                runBlockingTest {
                    val encrypted = encrypter.encrypt(sampleByteArray)
                    val decrypted = encrypter.decrypt(encrypted)

                    expectThat(decrypted) contentEquals sampleByteArray
                    expectThat(decrypted) propertiesAreEqualTo sampleByteArray
                }
            }
        }
}
