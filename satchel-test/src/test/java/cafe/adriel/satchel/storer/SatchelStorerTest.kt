package cafe.adriel.satchel.storer

import cafe.adriel.satchel.core.ktx.serialize
import cafe.adriel.satchel.storer.file.FileSatchelStorer
import cafe.adriel.satchel.util.SampleData
import com.google.crypto.tink.aead.AeadConfig
import io.mockk.spyk
import java.io.File
import java.util.UUID
import java.util.stream.Stream
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import strikt.api.expectThat
import strikt.assertions.contentEquals
import strikt.assertions.propertiesAreEqualTo

class SatchelStorerTest {

    init {
        AeadConfig.register()
    }

    private lateinit var fileStorer: FileSatchelStorer

    private val randomFile: File
        get() = File.createTempFile(UUID.randomUUID().toString(), "")

    private val sampleByteArray = SampleData.allSupportedTypes.serialize()

    @BeforeEach
    fun setup() {
        fileStorer = spyk(FileSatchelStorer(randomFile))
    }

    @TestFactory
    fun `when save a byte array then load correctly`(): Stream<DynamicTest> =
        Stream.of(
            fileStorer
        ).map { storer ->
            dynamicTest(storer::class.simpleName) {
                runBlockingTest {
                    storer.save(sampleByteArray)
                    val loaded = storer.load()

                    expectThat(loaded) contentEquals sampleByteArray
                    expectThat(loaded) propertiesAreEqualTo sampleByteArray
                }
            }
        }
}
