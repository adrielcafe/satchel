package cafe.adriel.satchel.serializer

import cafe.adriel.satchel.serializer.base64.jvm.Base64SatchelSerializer
import cafe.adriel.satchel.serializer.gzip.GzipSatchelSerializer
import cafe.adriel.satchel.serializer.kryo.KryoSatchelSerializer
import cafe.adriel.satchel.serializer.protobuf.ProtobufSatchelSerializer
import cafe.adriel.satchel.serializer.raw.RawSatchelSerializer
import cafe.adriel.satchel.util.SampleData
import java.util.stream.Stream
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class SatchelSerializerTest {

    private val rawSerializer = RawSatchelSerializer
    private val gzipSerializer = GzipSatchelSerializer
    private val base64Serializer = Base64SatchelSerializer
    private val kryoSerializer = KryoSatchelSerializer
    private val protobufSerializer = ProtobufSatchelSerializer
    // TODO waiting for fix https://github.com/google/flatbuffers/issues/5944
//    private val flatbuffersSerializer = FlatbuffersSatchelSerializer

    @TestFactory
    fun `when serialize primitives then deserialize correctly`(): Stream<DynamicTest> =
        Stream.of(
            rawSerializer,
            gzipSerializer,
            base64Serializer,
            kryoSerializer,
            protobufSerializer
            // TODO waiting for fix https://github.com/google/flatbuffers/issues/5944
//            flatbuffersSerializer
        ).map { serializer ->
            DynamicTest.dynamicTest(serializer::class.simpleName) {
                runBlockingTest {
                    val serialized = serializer.serialize(SampleData.primitives)
                    val deserialized = serializer.deserialize(serialized)

                    expectThat(deserialized) isEqualTo SampleData.primitives
                }
            }
        }

    @TestFactory
    fun `when serialize list of primitives then deserialize correctly`(): Stream<DynamicTest> =
        Stream.of(
            rawSerializer,
            gzipSerializer,
            base64Serializer,
            kryoSerializer,
            protobufSerializer
            // TODO waiting for fix https://github.com/google/flatbuffers/issues/5944
//            flatbuffersSerializer
        ).map { serializer ->
            DynamicTest.dynamicTest(serializer::class.simpleName) {
                runBlockingTest {
                    val serialized = serializer.serialize(SampleData.listOfPrimitives)
                    val deserialized = serializer.deserialize(serialized)

                    expectThat(deserialized) isEqualTo SampleData.listOfPrimitives
                }
            }
        }

    @TestFactory
    fun `when serialize serializable classes then deserialize correctly`(): Stream<DynamicTest> =
        Stream.of(
            rawSerializer,
            gzipSerializer,
            base64Serializer,
            kryoSerializer
        ).map { serializer ->
            DynamicTest.dynamicTest(serializer::class.simpleName) {
                runBlockingTest {
                    val serialized = serializer.serialize(SampleData.serializableClasses)
                    val deserialized = serializer.deserialize(serialized)

                    expectThat(deserialized) isEqualTo SampleData.serializableClasses
                }
            }
        }

    @TestFactory
    fun `when serialize list of serializable classes then deserialize correctly`(): Stream<DynamicTest> =
        Stream.of(
            rawSerializer,
            gzipSerializer,
            base64Serializer,
            kryoSerializer
        ).map { serializer ->
            DynamicTest.dynamicTest(serializer::class.simpleName) {
                runBlockingTest {
                    val serialized = serializer.serialize(SampleData.listOfSerializableClasses)
                    val deserialized = serializer.deserialize(serialized)

                    expectThat(deserialized) isEqualTo SampleData.listOfSerializableClasses
                }
            }
        }
}
