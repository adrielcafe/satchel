package cafe.adriel.satchel.serializer.gzip

import cafe.adriel.satchel.ktx.deserialize
import cafe.adriel.satchel.ktx.serialize
import cafe.adriel.satchel.serializer.SatchelSerializer
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

object GzipSatchelSerializer : SatchelSerializer {

    override suspend fun serialize(data: Map<String, Any>): ByteArray =
        ByteArrayOutputStream().use { bytesStream ->
            GZIPOutputStream(bytesStream).use { gzipStream ->
                gzipStream.write(data.serialize())
            }
            bytesStream.toByteArray()
        }

    override fun deserialize(data: ByteArray): Map<String, Any> =
        GZIPInputStream(data.inputStream()).use { gzipStream ->
            gzipStream.readBytes().deserialize()
        }
}
