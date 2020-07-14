package cafe.adriel.satchel.serializer.kryo

import cafe.adriel.satchel.serializer.SatchelSerializer
import com.esotericsoftware.kryo.kryo5.Kryo
import com.esotericsoftware.kryo.kryo5.io.Input
import com.esotericsoftware.kryo.kryo5.io.Output

object KryoSatchelSerializer : SatchelSerializer {

    private const val INITIAL_BUFFER_SIZE = 1024
    private const val MAX_BUFFER_SIZE = -1

    private val kryo by lazy {
        Kryo().apply {
            isRegistrationRequired = false
        }
    }

    override suspend fun serialize(data: Map<String, Any>): ByteArray =
        Output(INITIAL_BUFFER_SIZE, MAX_BUFFER_SIZE).use {
            kryo.writeClassAndObject(it, data)
            it.buffer
        }

    override fun deserialize(data: ByteArray): Map<String, Any> =
        Input(data).use {
            kryo.readClassAndObject(it) as Map<String, Any>
        }
}
