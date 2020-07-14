package cafe.adriel.satchel.serializer.flatbuffers

import com.google.flatbuffers.FlexBuffers
import com.google.flatbuffers.FlexBuffersBuilder

internal fun FlexBuffersBuilder.putMap(map: Map<String, Any>) {
    val index = startMap()
    map.forEach { (key, value) ->
        when (value) {
            is Double -> putFloat(key, value)
            is Float -> putFloat(key, value)
            is Int -> putInt(key, value)
            is Long -> putInt(key, value)
            is Boolean -> putBoolean(key, value)
            is String -> putString(key, value)
            is List<*> -> putListValues(key, value.mapNotNull { it })
            else -> throw TypeCastException("Flatbuffers type not supported: ${value::class}")
        }
    }
    endMap(null, index)
}

private fun FlexBuffersBuilder.putListValues(key: String, values: List<Any>) {
    val index = startVector()
    values.forEach { value ->
        when (value) {
            is Double -> putFloat(value)
            is Float -> putFloat(value)
            is Int -> putInt(value)
            is Long -> putInt(value)
            is Boolean -> putBoolean(value)
            is String -> putString(value)
            else -> throw TypeCastException("Flatbuffers type not supported: $value")
        }
    }
    endVector(key, index, false, false)
}

internal fun FlexBuffers.Map.toStorage(): Map<String, Any> =
    mutableMapOf<String, Any>().also { map ->
        repeat(keys().size()) { keyIndex ->
            val key = keys().get(keyIndex).toString()
            map[key] = get(key).toAnyValue()
        }
    }

@OptIn(ExperimentalStdlibApi::class)
private fun FlexBuffers.Reference.toAnyValue(): Any =
    when {
        isVector -> buildList<Any> {
            val vector = asVector()
            repeat(vector.size()) { index ->
                vector.get(index)
                    .toAnyValue()
                    .let(::add)
            }
        }
        isFloat -> when (asFloat()) {
            in Float.MIN_VALUE..Float.MAX_VALUE -> asFloat().toFloat()
            else -> asFloat()
        }
        isInt -> when (asLong()) {
            in Int.MIN_VALUE..Int.MAX_VALUE -> asInt()
            else -> asLong()
        }
        isBoolean -> asBoolean()
        isString -> asString()
        else -> throw TypeCastException("Flatbuffers type not supported: $type")
    }
