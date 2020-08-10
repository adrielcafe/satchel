package cafe.adriel.satchel

import java.util.concurrent.ConcurrentHashMap

abstract class ConcurrentSatchelStorage : SatchelStorage {

    private companion object {
        const val CHECK_CLOSED_MESSAGE = "Satchel is closed"
    }

    @PublishedApi
    internal val storage: MutableMap<String, Any> = ConcurrentHashMap()

    override val keys: Set<String>
        get() = storage.keys

    override val size: Int
        get() = storage.size

    override val isEmpty: Boolean
        get() = storage.isEmpty()

    protected abstract fun onStorageChanged(event: SatchelEvent)

    override infix fun contains(key: String): Boolean =
        storage.containsKey(key)

    override infix fun getAny(key: String): Any? =
        storage[key]

    override operator fun <T : Any> set(key: String, value: T) {
        check(isClosed.not()) { CHECK_CLOSED_MESSAGE }

        storage[key] = value

        onStorageChanged(SatchelEvent.Set(key))
    }

    override fun <T : Any> setIfAbsent(key: String, value: T) {
        if (contains(key).not()) {
            set(key, value)
        }
    }

    override infix fun remove(key: String) {
        check(isClosed.not()) { CHECK_CLOSED_MESSAGE }

        storage.remove(key)
            ?.also { onStorageChanged(SatchelEvent.Remove(key)) }
    }

    override fun clear() {
        check(isClosed.not()) { CHECK_CLOSED_MESSAGE }

        storage.clear()

        onStorageChanged(SatchelEvent.Clear)
    }
}
