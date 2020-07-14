package cafe.adriel.satchel.ktx

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

fun Map<String, Any>.serialize(): ByteArray =
    ByteArrayOutputStream().use { bytesStream ->
        ObjectOutputStream(bytesStream).use { objectStream ->
            objectStream.writeObject(this)
        }
        bytesStream.toByteArray()
    }

fun ByteArray.deserialize(): Map<String, Any> =
    when {
        isEmpty() -> emptyMap()
        else -> ByteArrayInputStream(this).use { bytesStream ->
            ObjectInputStream(bytesStream).use { objectStream ->
                objectStream.readObject() as Map<String, Any>
            }
        }
    }
