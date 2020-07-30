package cafe.adriel.satchel.benchmark.storer

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.core.app.ApplicationProvider
import cafe.adriel.satchel.benchmark.randomFile
import cafe.adriel.satchel.benchmark.serializedSampleData
import cafe.adriel.satchel.storer.SatchelStorer
import cafe.adriel.satchel.storer.encryptedfile.EncryptedFileSatchelStorer
import cafe.adriel.satchel.storer.file.FileSatchelStorer
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test

class StorerWriteBenchmark {

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    @Test
    fun file() = runBenchmark(FileSatchelStorer(randomFile))

    @Test
    fun encryptedFile() = runBenchmark(
        EncryptedFileSatchelStorer.with(
            context = ApplicationProvider.getApplicationContext(),
            file = randomFile
        )
    )

    private fun runBenchmark(storer: SatchelStorer) {
        benchmarkRule.measureRepeated {
            runBlockingTest {
                storer.save(serializedSampleData)
            }
        }
    }
}
