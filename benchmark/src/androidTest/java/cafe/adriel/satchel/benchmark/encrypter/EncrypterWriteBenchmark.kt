package cafe.adriel.satchel.benchmark.encrypter

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.core.app.ApplicationProvider
import cafe.adriel.satchel.benchmark.serializedSampleData
import cafe.adriel.satchel.encrypter.SatchelEncrypter
import cafe.adriel.satchel.encrypter.cipher.CipherSatchelEncrypter
import cafe.adriel.satchel.encrypter.cipher.CipherSatchelEncrypter.CipherKey
import cafe.adriel.satchel.encrypter.jose4j.Jose4jSatchelEncrypter
import cafe.adriel.satchel.encrypter.tink.android.TinkSatchelEncrypter
import com.google.crypto.tink.aead.AeadConfig
import javax.crypto.KeyGenerator
import kotlinx.coroutines.test.runBlockingTest
import org.jose4j.jwk.RsaJwkGenerator
import org.junit.Rule
import org.junit.Test

class EncrypterWriteBenchmark {

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    init {
        AeadConfig.register()
    }

    @Test
    fun cipher() = runBenchmark(
        CipherSatchelEncrypter.with(
            cipherKey = CipherKey.SecretKey(
                key = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()
            )
        )
    )

    @Test
    fun tink() = runBenchmark(
        TinkSatchelEncrypter.with(context = ApplicationProvider.getApplicationContext())
    )

    @Test
    fun jose4j() = runBenchmark(
        Jose4jSatchelEncrypter.with(jwk = RsaJwkGenerator.generateJwk(2048))
    )

    private fun runBenchmark(encrypter: SatchelEncrypter) {
        benchmarkRule.measureRepeated {
            runBlockingTest {
                encrypter.encrypt(serializedSampleData)
            }
        }
    }
}
