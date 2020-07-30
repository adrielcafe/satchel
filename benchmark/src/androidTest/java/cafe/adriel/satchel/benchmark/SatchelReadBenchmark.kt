package cafe.adriel.satchel.benchmark

import android.content.Context
import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.core.app.ApplicationProvider
import cafe.adriel.satchel.Satchel
import cafe.adriel.satchel.encrypter.none.NoneSatchelEncrypter
import cafe.adriel.satchel.serializer.raw.RawSatchelSerializer
import cafe.adriel.satchel.storer.file.FileSatchelStorer
import com.orhanobut.hawk.Hawk
import com.tencent.mmkv.MMKV
import io.paperdb.Paper
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.Rule
import org.junit.Test

class SatchelReadBenchmark {

    private val coroutineScope = TestCoroutineScope()

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    @Test
    fun satchel() {
        val storer = FileSatchelStorer(randomFile)
        val serializer = RawSatchelSerializer
        val encrypter = NoneSatchelEncrypter

        val writeSatchel = Satchel.with(storer, serializer, encrypter, coroutineScope)

        sampleData.forEach { (key, value) ->
            writeSatchel[key] = value
        }

        benchmarkRule.measureRepeated {
            val readSatchel = Satchel.with(storer, serializer, encrypter, coroutineScope)
            sampleData.forEach { (key, _) ->
                val storedValue = readSatchel.get<String>(key)
            }
        }
    }

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

        benchmarkRule.measureRepeated {
            val readSharedPreferences = ApplicationProvider
                .getApplicationContext<Context>()
                .getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
            sampleData.forEach { (key, _) ->
                val storedValue = readSharedPreferences.getString(key, null)
            }
        }
    }

    @Test
    fun mmkv() {
        MMKV.initialize(ApplicationProvider.getApplicationContext<Context>())
        val id = randomName
        val writeMmkv = MMKV.mmkvWithID(id, MMKV.SINGLE_PROCESS_MODE)
        sampleData.forEach { (key, value) ->
            writeMmkv.encode(key, value)
        }

        benchmarkRule.measureRepeated {
            val readMmkv = MMKV.mmkvWithID(id)
            sampleData.forEach { (key, _) ->
                val storedValue = readMmkv.decodeString(key)
            }
        }
    }

    @Test
    fun paper() {
        Paper.init(ApplicationProvider.getApplicationContext())
        val bookName = randomName
        val writePaper = Paper.book(bookName)
        sampleData.forEach { (key, value) ->
            writePaper.write(key, value)
        }

        benchmarkRule.measureRepeated {
            val readPaper = Paper.book(bookName)
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
}
