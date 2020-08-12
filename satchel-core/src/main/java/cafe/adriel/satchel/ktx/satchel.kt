package cafe.adriel.satchel.ktx

import cafe.adriel.satchel.SatchelStorage
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

inline fun <reified T : Any> SatchelStorage.value(key: String): ReadWriteProperty<Any, T?> =
    object : ReadWriteProperty<Any, T?> {
        override fun getValue(thisRef: Any, property: KProperty<*>): T? =
            get(key)

        override fun setValue(thisRef: Any, property: KProperty<*>, value: T?) =
            when (value) {
                null -> remove(key)
                else -> set(key, value)
            }
    }

inline fun <reified T : Any> SatchelStorage.value(key: String, defaultValue: T): ReadWriteProperty<Any, T> =
    object : ReadWriteProperty<Any, T> {
        override fun getValue(thisRef: Any, property: KProperty<*>): T =
            getOrDefault(key, defaultValue)

        override fun setValue(thisRef: Any, property: KProperty<*>, value: T) =
            set(key, value)
    }

inline infix operator fun <reified T : Any> SatchelStorage.get(key: String): T? =
    getAny(key) as? T

inline fun <reified T : Any> SatchelStorage.getOrDefault(key: String, defaultValue: T): T =
    get(key) ?: defaultValue

inline fun <reified T : Any> SatchelStorage.getOrDefault(key: String, defaultValue: () -> T): T =
    get(key) ?: defaultValue()

inline fun <reified T : Any> SatchelStorage.getOrSet(key: String, defaultValue: T): T =
    get(key) ?: run {
        set(key, defaultValue)
        defaultValue
    }

inline fun <reified T : Any> SatchelStorage.getOrSet(key: String, defaultValue: () -> T): T =
    get(key) ?: run {
        val value = defaultValue()
        set(key, value)
        value
    }
