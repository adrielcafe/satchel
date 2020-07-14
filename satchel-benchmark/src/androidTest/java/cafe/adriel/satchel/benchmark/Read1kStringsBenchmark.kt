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
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import org.jose4j.jwk.RsaJwkGenerator
import org.junit.Rule
import org.junit.Test

class Read1kStringsBenchmark {

    private val sampleData = Array(1_000) { "Key $it" to "Value $it" }

    private val randomName: String
        get() = UUID.randomUUID().toString()

    private val randomFile: File
        get() = File.createTempFile(UUID.randomUUID().toString(), "")

    private val coroutineScope = TestCoroutineScope()
    private val coroutineDispatcher = TestCoroutineDispatcher()

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
        val preferenceName = randomName
        ApplicationProvider
            .getApplicationContext<Context>()
            .getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
            .edit()
            .apply {
                sampleData.forEach { (key, value) ->
                    putString(key, value)
                }
            }
            .commit()

        val readSharedPreferences = ApplicationProvider
            .getApplicationContext<Context>()
            .getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
        benchmarkRule.measureRepeated {
            sampleData.forEach { (key, _) ->
                val storedValue = readSharedPreferences.getString(key, null)
            }
        }
    }

    @Test
    fun encryptedSharedPreferences() {
        val preferenceName = randomName
        EncryptedSharedPreferences
            .create(
                preferenceName,
                MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
                ApplicationProvider.getApplicationContext(),
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            ).edit()
            .apply {
                sampleData.forEach { (key, value) ->
                    putString(key, value)
                }
            }
            .commit()

        val readSharedPreferences = EncryptedSharedPreferences.create(
            preferenceName,
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
            ApplicationProvider.getApplicationContext(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        benchmarkRule.measureRepeated {
            sampleData.forEach { (key, _) ->
                val storedValue = readSharedPreferences.getString(key, null)
            }
        }
    }

    @Test
    fun mmkv() = runMmkvBenchmark(cryptoKey = null)

    @Test
    fun mmkvEncrypted() = runMmkvBenchmark(cryptoKey = "satchel rules")

    @Test
    fun paper() {
        Paper.init(ApplicationProvider.getApplicationContext())
        val bookName = randomName
        val writePaper = Paper.book(bookName)
        sampleData.forEach { (key, value) ->
            writePaper.write(key, value)
        }

        val readPaper = Paper.book(bookName)
        benchmarkRule.measureRepeated {
            sampleData.forEach { (key, _) ->
                val storedValue = readPaper.read<String>(key)
            }
        }
    }

    @Test
    fun hawk() {
        Hawk.init(ApplicationProvider.getApplicationContext()).build()
        sampleData.forEach { (key, value) ->
            Hawk.put(key, value)
        }

        benchmarkRule.measureRepeated {
            sampleData.forEach { (key, _) ->
                val storedValue = Hawk.get<String>(key)
            }
        }
    }

    private fun runSatchelBenchmark(
        storer: SatchelStorer,
        serializer: SatchelSerializer,
        encrypter: SatchelEncrypter
    ) {
        val writeSatchel = Satchel.with(storer, serializer, encrypter, coroutineScope, coroutineDispatcher)
        sampleData.forEach { (key, value) ->
            writeSatchel[key] = value
        }

        val readSatchel = Satchel.with(storer, serializer, encrypter, coroutineScope)
        benchmarkRule.measureRepeated {
            sampleData.forEach { (key, _) ->
                val storedValue = readSatchel.get<String>(key)
            }
        }
    }

    private fun runMmkvBenchmark(cryptoKey: String?) {
        MMKV.initialize(ApplicationProvider.getApplicationContext<Context>())
        val id = randomName
        val writeMmkv = MMKV.mmkvWithID(id, MMKV.SINGLE_PROCESS_MODE, cryptoKey)
        sampleData.forEach { (key, value) ->
            writeMmkv.encode(key, value)
        }

        val readMmkv = MMKV.mmkvWithID(id)
        benchmarkRule.measureRepeated {
            sampleData.forEach { (key, _) ->
                val storedValue = readMmkv.decodeString(key)
            }
        }
    }
}
