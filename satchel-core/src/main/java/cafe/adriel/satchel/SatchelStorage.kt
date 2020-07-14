package cafe.adriel.satchel

import java.util.concurrent.ConcurrentHashMap

abstract class SatchelStorage {

    @PublishedApi
    internal val storage: MutableMap<String, Any> = ConcurrentHashMap()

    protected abstract fun onStorageChanged(event: SatchelEvent)

    abstract fun addListener(listener: (SatchelEvent) -> Unit)

    abstract fun removeListener(listener: (SatchelEvent) -> Unit)

    abstract fun clearListeners()

    val keys: Set<String>
        get() = storage.keys

    val size: Int
        get() = storage.size

    val isEmpty: Boolean
        get() = storage.isEmpty()

    inline infix operator fun <reified T : Any> get(key: String): T? =
        storage[key] as? T

    inline fun <reified T : Any> getOrDefault(key: String, defaultValue: T): T =
        get(key) ?: defaultValue

    inline fun <reified T : Any> getOrSet(key: String, defaultValue: T): T =
        get(key) ?: run {
            set(key, defaultValue)
            defaultValue
        }

    operator fun <T : Any> set(key: String, value: T) {
        storage[key] = value
        onStorageChanged(SatchelEvent.Set(key))
    }

    fun <T : Any> setIfAbsent(key: String, value: T) {
        if (contains(key).not()) {
            set(key, value)
        }
    }

    infix fun contains(key: String): Boolean =
        storage.containsKey(key)

    infix fun remove(key: String) {
        storage.remove(key)
            ?.also { onStorageChanged(SatchelEvent.Remove(key)) }
    }

    fun clear() {
        storage.clear()
        onStorageChanged(SatchelEvent.Clear)
    }
}
