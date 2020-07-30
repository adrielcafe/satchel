package cafe.adriel.satchel.sample

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import cafe.adriel.satchel.Satchel
import cafe.adriel.satchel.encrypter.none.NoneSatchelEncrypter
import cafe.adriel.satchel.ktx.get
import cafe.adriel.satchel.serializer.raw.RawSatchelSerializer
import cafe.adriel.satchel.storer.file.FileSatchelStorer
import java.io.File
import kotlin.random.Random
import kotlin.random.nextInt
import kotlinx.android.synthetic.main.activity_sample.*
import kotlinx.coroutines.delay

class SampleActivity : AppCompatActivity(R.layout.activity_sample) {

    private companion object {
        const val PRINT_DELAY = 1_000L
        const val ITEM_COUNT = 10
    }

    private val satchelFile by lazy {
        File(filesDir, "satchel.storage")
    }

    private val satchel by lazy {
        Satchel.with(
            storer = FileSatchelStorer(satchelFile),
            encrypter = NoneSatchelEncrypter,
            serializer = RawSatchelSerializer
        )
    }

    init {
        lifecycleScope.launchWhenResumed {
            outputView.text = null
            runSample()
        }
    }

    private suspend fun runSample() {
        satchel.addListener(lifecycleScope) { event ->
            Log.e("SATCHEL EVENT", event.toString())
        }

        if (satchel.isEmpty) {
            "> Storage is empty".print()
        } else {
            "> Found ${satchel.size} stored values:".print()
            satchel.keys.printAll()

            "> Clearing storage".print()
            satchel.clear()
        }

        "> Adding values:".print()
        repeat(ITEM_COUNT) { i ->
            satchel["Key $i"] = "Value $i"
        }
        satchel.keys.printAll()

        "> Removing random values:".print()
        satchel.keys
            .shuffled()
            .take(Random.nextInt(1 until satchel.size))
            .apply { printAll() }
            .forEach(satchel::remove)

        "> Retrieving ${satchel.size} values:".print()
        satchel.keys.printAll()
    }

    private suspend fun String.print() {
        delay(PRINT_DELAY)
        outputView.text = "${outputView.text}$this\n\n"
    }

    private suspend fun Collection<String>.printAll() =
        joinToString(separator = "\n") { key -> "  $key: ${satchel.get<String>(key)}" }
            .print()
}
