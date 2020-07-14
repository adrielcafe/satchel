package cafe.adriel.satchel.benchmark

import android.content.Context
import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import androidx.test.core.app.ApplicationProvider
import cafe.adriel.satchel.Satchel
import cafe.adriel.satchel.encrypter.SatchelEncrypter
import cafe.adriel.satchel.encrypter.cipher.CipherSatchelEncrypter
import cafe.adriel.satchel.encrypter.cipher.CipherSatchelEncrypter.CipherKey
import cafe.adriel.satchel.encrypter.jose4j.Jose4jSatchelEncrypter
import cafe.adriel.satchel.encrypter.none.NoneSatchelEncrypter
import cafe.adriel.satchel.encrypter.tink.android.TinkSatchelEncrypter
import cafe.adriel.satchel.serializer.SatchelSerializer
import cafe.adriel.satchel.serializer.base64.android.Base64SatchelSerializer as Base64AndroidSatchelSerializer
import cafe.adriel.satchel.serializer.base64.jvm.Base64SatchelSerializer as Base64JvmSatchelSerializer
import cafe.adriel.satchel.serializer.flatbuffers.FlatbuffersSatchelSerializer
import cafe.adriel.satchel.serializer.gzip.GzipSatchelSerializer
import cafe.adriel.satchel.serializer.kryo.KryoSatchelSerializer
import cafe.adriel.satchel.serializer.protobuf.ProtobufSatchelSerializer
import cafe.adriel.satchel.serializer.raw.RawSatchelSerializer
import cafe.adriel.satchel.storer.SatchelStorer
import cafe.adriel.satchel.storer.encryptedfile.EncryptedFileSatchelStorer
import cafe.adriel.satchel.storer.file.FileSatchelStorer
import com.orhanobut.hawk.Hawk
import com.tencent.mmkv.MMKV
import io.paperdb.Paper
import java.io.File
import java.util.UUID
import javax.crypto.KeyGenerator
import kotlinx.coroutines.test.TestCoroutineScope
import org.jose4j.jwk.RsaJwkGenerator
import org.junit.Rule
import org.junit.Test

class Write1kStringsBenchmark {

    private val sampleData = Array(1_000) { "Key $it" to "Value $it" }

    private val randomName: String
        get() = UUID.randomUUID().toString()

    private val randomFile: File
        get() = File.createTempFile(randomName, "")

    private val coroutineScope = TestCoroutineScope()

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    // Satchel core

    @Test
    fun coreImplementation() = runSatchelBenchmark(
        storer = FileSatchelStorer(randomFile),
        serializer = RawSatchelSerializer,
        encrypter = NoneSatchelEncrypter
    )

    // Satchel storers

    @Test
    fun encryptedFileStorer() = runSatchelBenchmark(
        storer = EncryptedFileSatchelStorer.with(
            context = ApplicationProvider.getApplicationContext(),
            file = randomFile
        ),
        serializer = RawSatchelSerializer,
        encrypter = NoneSatchelEncrypter
    )

    // Satchel encrypters

    @Test
    fun cipherEncrypter() = runSatchelBenchmark(
        storer = FileSatchelStorer(randomFile),
        serializer = RawSatchelSerializer,
        encrypter = CipherSatchelEncrypter.with(
            cipherKey = CipherKey.SecretKey(
                key = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()
            )
        )
    )

    @Test
    fun tinkEncrypter() = runSatchelBenchmark(
        storer = FileSatchelStorer(randomFile),
        serializer = RawSatchelSerializer,
        encrypter = TinkSatchelEncrypter.with(context = ApplicationProvider.getApplicationContext())
    )

    @Test
    fun jose4jEncrypter() = runSatchelBenchmark(
        storer = FileSatchelStorer(randomFile),
        serializer = RawSatchelSerializer,
        encrypter = Jose4jSatchelEncrypter.with(jwk = RsaJwkGenerator.generateJwk(2048))
    )

    // Satchel serializers

    @Test
    fun gzipSerializer() = runSatchelBenchmark(
        storer = FileSatchelStorer(randomFile),
        serializer = GzipSatchelSerializer,
        encrypter = NoneSatchelEncrypter
    )

    @Test
    fun base64JvmSerializer() = runSatchelBenchmark(
        storer = FileSatchelStorer(randomFile),
        serializer = Base64JvmSatchelSerializer,
        encrypter = NoneSatchelEncrypter
    )

    @Test
    fun base64AndroidSerializer() = runSatchelBenchmark(
        storer = FileSatchelStorer(randomFile),
        serializer = Base64AndroidSatchelSerializer,
        encrypter = NoneSatchelEncrypter
    )

    @Test
    fun protobufSerializer() = runSatchelBenchmark(
        storer = FileSatchelStorer(randomFile),
        serializer = ProtobufSatchelSerializer,
        encrypter = NoneSatchelEncrypter
    )

    @Test
    fun flatbuffersSerializer() = runSatchelBenchmark(
        storer = FileSatchelStorer(randomFile),
        serializer = FlatbuffersSatchelSerializer,
        encrypter = NoneSatchelEncrypter
    )

    @Test
    fun kryoSerializer() = runSatchelBenchmark(
        storer = FileSatchelStorer(randomFile),
        serializer = KryoSatchelSerializer,
        encrypter = NoneSatchelEncrypter
    )

    // Third-party libraries

    @Test
    fun sharedPreferences() {
        val editor = ApplicationProvider
            .getApplicationContext<Context>()
            .getSharedPreferences(randomName, Context.MODE_PRIVATE)
            .edit()

        benchmarkRule.measureRepeated {
            sampleData.forEach { (key, value) ->
                editor.putString(key, value)
            }
            editor.apply()
        }
    }

    @Test
    fun encryptedSharedPreferences() {
        val editor = EncryptedSharedPreferences
            .create(
                randomName,
                MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
                ApplicationProvider.getApplicationContext(),
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            .edit()

        benchmarkRule.measureRepeated {
            sampleData.forEach { (key, value) ->
                editor.putString(key, value)
            }
            editor.apply()
        }
    }

    @Test
    fun mmkv() = runMmkvBenchmark(cryptoKey = null)

    @Test
    fun mmkvEncrypted() = runMmkvBenchmark(cryptoKey = "satchel rules")

    @Test
    fun paper() {
        Paper.init(ApplicationProvider.getApplicationContext())
        val paper = Paper.book(randomName)

        benchmarkRule.measureRepeated {
            sampleData.forEach { (key, value) ->
                paper.write(key, value)
            }
        }
    }

    @Test
    fun hawk() {
        Hawk.init(ApplicationProvider.getApplicationContext()).build()

        benchmarkRule.measureRepeated {
            sampleData.forEach { (key, value) ->
                Hawk.put(key, value)
            }
        }
    }

    private fun runSatchelBenchmark(
        storer: SatchelStorer,
        serializer: SatchelSerializer,
        encrypter: SatchelEncrypter
    ) {
        val satchel = Satchel.with(storer, serializer, encrypter, coroutineScope)

        benchmarkRule.measureRepeated {
            sampleData.forEach { (key, value) ->
                satchel[key] = value
            }
        }
    }

    private fun runMmkvBenchmark(cryptoKey: String?) {
        MMKV.initialize(ApplicationProvider.getApplicationContext<Context>())
        val mmkv = MMKV.mmkvWithID(randomName, MMKV.SINGLE_PROCESS_MODE, cryptoKey).apply { async() }

        benchmarkRule.measureRepeated {
            sampleData.forEach { (key, value) ->
                mmkv.encode(key, value)
            }
        }
    }
}
