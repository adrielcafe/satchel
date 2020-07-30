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

class SatchelWriteBenchmark {

    private val coroutineScope = TestCoroutineScope()

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    @Test
    fun satchel() {
        val satchel = Satchel.with(
            storer = FileSatchelStorer(randomFile),
            serializer = RawSatchelSerializer,
            encrypter = NoneSatchelEncrypter,
            storageScope = coroutineScope
        )

        benchmarkRule.measureRepeated {
            sampleData.forEach { (key, value) ->
                satchel[key] = value
            }
        }
    }

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
    fun mmkv() {
        MMKV.initialize(ApplicationProvider.getApplicationContext<Context>())
        val mmkv = MMKV.mmkvWithID(randomName, MMKV.SINGLE_PROCESS_MODE).apply { async() }

        benchmarkRule.measureRepeated {
            sampleData.forEach { (key, value) ->
                mmkv.encode(key, value)
            }
        }
    }

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
}
