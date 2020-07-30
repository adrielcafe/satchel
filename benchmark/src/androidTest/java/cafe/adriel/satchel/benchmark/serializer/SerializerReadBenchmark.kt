package cafe.adriel.satchel.benchmark.serializer

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import cafe.adriel.satchel.benchmark.sampleData
import cafe.adriel.satchel.ktx.serialize
import cafe.adriel.satchel.serializer.SatchelSerializer
import cafe.adriel.satchel.serializer.base64.android.Base64SatchelSerializer as AndroidBase64SatchelSerializer
import cafe.adriel.satchel.serializer.base64.jvm.Base64SatchelSerializer as JvmBase64SatchelSerializer
import cafe.adriel.satchel.serializer.gzip.GzipSatchelSerializer
import cafe.adriel.satchel.serializer.kryo.KryoSatchelSerializer
import cafe.adriel.satchel.serializer.protobuf.ProtobufSatchelSerializer
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test

class SerializerReadBenchmark {

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    @Test
    fun gzip() = runBenchmark(GzipSatchelSerializer)

    @Test
    fun base64Jvm() = runBenchmark(JvmBase64SatchelSerializer)

    @Test
    fun base64Android() = runBenchmark(AndroidBase64SatchelSerializer)

    @Test
    fun protobuf() = runBenchmark(ProtobufSatchelSerializer)

    // TODO waiting for fix https://github.com/google/flatbuffers/issues/5944
//    @Test
//    fun flatbuffers() = runBenchmark(FlatbuffersSatchelSerializer)

    @Test
    fun kryo() = runBenchmark(KryoSatchelSerializer)

    private fun runBenchmark(serializer: SatchelSerializer) = runBlockingTest {
        val data = serializer.serialize(sampleData)

        benchmarkRule.measureRepeated {
            serializer.deserialize(data)
        }
    }
}
