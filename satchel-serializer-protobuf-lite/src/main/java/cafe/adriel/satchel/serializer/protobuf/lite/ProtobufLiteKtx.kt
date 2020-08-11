package cafe.adriel.satchel.serializer.protobuf.lite

import cafe.adriel.satchel.serializer.protobuf.lite.proto.SatchelProto.Value
import cafe.adriel.satchel.serializer.protobuf.lite.proto.SatchelProto.Value.Type
import cafe.adriel.satchel.serializer.protobuf.lite.proto.SatchelProto.Value.Type.TypeCase

internal fun Any.toProtoValue(): Value =
    if (this is List<*>) {
        Value.newBuilder()
            .also { builder ->
                this.forEach { item ->
                    item
                        ?.let(::getProtoType)
                        ?.let(builder::addMultiValue)
                }
            }
            .build()
    } else {
        Value.newBuilder()
            .setSingleValue(getProtoType(this))
            .build()
    }

private fun getProtoType(value: Any): Type =
    Type.newBuilder()
        .apply {
            when (value) {
                is Double -> doubleValue = value
                is Float -> floatValue = value
                is Int -> intValue = value
                is Long -> longValue = value
                is Boolean -> boolValue = value
                is String -> stringValue = value
                else -> throw TypeCastException("Protobuf type not supported: ${value::class}")
            }
        }
        .build()

internal fun Value.toAnyValue(): Any =
    if (hasSingleValue()) {
        singleValue.toSingleValue()
    } else {
        multiValueList.toListValue()
    }

internal fun Type.toSingleValue(): Any =
    when (typeCase) {
        TypeCase.DOUBLE_VALUE -> doubleValue
        TypeCase.FLOAT_VALUE -> floatValue
        TypeCase.INT_VALUE -> intValue
        TypeCase.LONG_VALUE -> longValue
        TypeCase.BOOL_VALUE -> boolValue
        TypeCase.STRING_VALUE -> stringValue
        else -> throw TypeCastException("Protobuf type not supported: $typeCase")
    }

internal fun List<Type>.toListValue(): List<Any> =
    mapNotNull { it.toSingleValue() }
