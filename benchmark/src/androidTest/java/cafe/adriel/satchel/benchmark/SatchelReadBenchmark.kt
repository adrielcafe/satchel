package cafe.adriel.satchel.benchmark

import android.content.Context
import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.core.app.ApplicationProvider
import cafe.adriel.satchel.Satchel
import cafe.adriel.satchel.encrypter.bypass.BypassSatchelEncrypter
import cafe.adriel.satchel.ktx.get
import cafe.adriel.satchel.serializer.raw.RawSatchelSerializer
import cafe.adriel.satchel.storer.file.FileSatchelStorer
import com.orhanobut.hawk.Hawk
import com.tencent.mmkv.MMKV
import io.paperdb.Paper
import org.junit.Rule
import org.junit.Test

class SatchelReadBenchmark {

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    @Test
    fun satchel() {
        val satchel = Satchel.with(FileSatchelStorer(randomFile), RawSatchelSerializer, BypassSatchelEncrypter)

        sampleData.forEach { (key, value) ->
            satchel[key] = value
        }

        benchmarkRule.measureRepeated {
            sampleData.forEach { (key, _) ->
                val storedValue = satchel.get<String>(key)
            }
        }
    }

    @Test
    fun sharedPreferences() {
        val sharedPreferences = ApplicationProvider
            .getApplicationContext<Context>()
            .getSharedPreferences(randomName, Context.MODE_PRIVATE)

        sharedPreferences.edit()
            .apply {
                sampleData.forEach { (key, value) ->
                    putString(key, value)
                }
            }
            .commit()

        benchmarkRule.measureRepeated {
            sampleData.forEach { (key, _) ->
                val storedValue = sharedPreferences.getString(key, null)
            }
        }
    }

    @Test
    fun mmkv() {
        MMKV.initialize(ApplicationProvider.getApplicationContext<Context>())
        val mmkv = MMKV.mmkvWithID(randomName, MMKV.SINGLE_PROCESS_MODE)

        sampleData.forEach { (key, value) ->
            mmkv.encode(key, value)
        }

        benchmarkRule.measureRepeated {
            sampleData.forEach { (key, _) ->
                val storedValue = mmkv.decodeString(key)
            }
        }
    }

    @Test
    fun paper() {
        Paper.init(ApplicationProvider.getApplicationContext())
        val paper = Paper.book(randomName)

        sampleData.forEach { (key, value) ->
            paper.write(key, value)
        }

        benchmarkRule.measureRepeated {
            sampleData.forEach { (key, _) ->
                val storedValue = paper.read<String>(key)
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
