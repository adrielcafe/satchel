package cafe.adriel.satchel

import java.io.Closeable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

interface SatchelStorage : Closeable {

    val keys: Set<String>

    val size: Int

    val isEmpty: Boolean

    val isClosed: Boolean

    fun addListener(
        scope: CoroutineScope,
        dispatcher: CoroutineDispatcher = Dispatchers.Main,
        listener: suspend (SatchelEvent) -> Unit
    )

    infix fun contains(key: String): Boolean

    infix fun getAny(key: String): Any?

    operator fun <T : Any> set(key: String, value: T)

    fun <T : Any> setIfAbsent(key: String, value: T)

    infix fun remove(key: String)

    fun clear()
}
