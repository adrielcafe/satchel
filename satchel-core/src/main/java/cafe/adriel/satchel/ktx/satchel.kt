package cafe.adriel.satchel.ktx

import cafe.adriel.satchel.SatchelStorage

inline infix operator fun <reified T : Any> SatchelStorage.get(key: String): T? =
    getAny(key) as? T

inline fun <reified T : Any> SatchelStorage.getOrDefault(key: String, defaultValue: T): T =
    get(key) ?: defaultValue

inline fun <reified T : Any> SatchelStorage.getOrElse(key: String, defaultValue: () -> T): T =
    get(key) ?: defaultValue()

inline fun <reified T : Any> SatchelStorage.getOrSet(key: String, defaultValue: T): T =
    get(key) ?: run {
        set(key, defaultValue)
        defaultValue
    }
