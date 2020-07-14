package cafe.adriel.satchel

import cafe.adriel.satchel.encrypter.SatchelEncrypter
import cafe.adriel.satchel.encrypter.none.NoneSatchelEncrypter
import cafe.adriel.satchel.serializer.SatchelSerializer
import cafe.adriel.satchel.serializer.raw.RawSatchelSerializer
import cafe.adriel.satchel.storer.SatchelStorer
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class Satchel private constructor(
    private val storer: SatchelStorer,
    private val serializer: SatchelSerializer,
    private val encrypter: SatchelEncrypter,
    storageScope: CoroutineScope,
    saveDispatcher: CoroutineContext,
    eventDispatcher: CoroutineContext
) : SatchelStorage() {

    companion object {

        lateinit var storage: SatchelStorage
            private set

        val isInitialized: Boolean
            get() = ::storage.isInitialized

        fun init(
            storer: SatchelStorer,
            serializer: SatchelSerializer = RawSatchelSerializer,
            encrypter: SatchelEncrypter = NoneSatchelEncrypter,
            storageScope: CoroutineScope = GlobalScope,
            saveDispatcher: CoroutineContext = Dispatchers.IO,
            eventDispatcher: CoroutineContext = Dispatchers.Main
        ) {
            storage = with(storer, serializer, encrypter, storageScope, saveDispatcher, eventDispatcher)
        }

        fun with(
            storer: SatchelStorer,
            serializer: SatchelSerializer = RawSatchelSerializer,
            encrypter: SatchelEncrypter = NoneSatchelEncrypter,
            storageScope: CoroutineScope = GlobalScope,
            saveDispatcher: CoroutineContext = Dispatchers.IO,
            eventDispatcher: CoroutineContext = Dispatchers.Main
        ): SatchelStorage =
            Satchel(storer, serializer, encrypter, storageScope, saveDispatcher, eventDispatcher)
    }

    private val saveChannel = Channel<Unit>(Channel.CONFLATED)
    private val saveMutex = Mutex()

    // TODO migrate to SharedFlow when released
    private val eventChannel = Channel<SatchelEvent>(Channel.RENDEZVOUS)
    private val eventListeners = mutableSetOf<(SatchelEvent) -> Unit>()

    init {
        storage += loadData()

        saveChannel
            .consumeAsFlow()
            .map { storage.toMap() }
            .onEach(::saveData)
            .flowOn(saveDispatcher)
            .launchIn(storageScope)

        eventChannel
            .consumeAsFlow()
            .onEach { event -> eventListeners.forEach { it(event) } }
            .flowOn(eventDispatcher)
            .launchIn(storageScope)
            .invokeOnCompletion {
                // TODO needs more tests
                clearListeners()
            }
    }

    override fun onStorageChanged(event: SatchelEvent) {
        saveChannel.sendBlocking(Unit)
        eventChannel.offer(event)
    }

    override fun addListener(listener: (SatchelEvent) -> Unit) {
        eventListeners += listener
    }

    override fun removeListener(listener: (SatchelEvent) -> Unit) {
        eventListeners -= listener
    }

    override fun clearListeners() =
        eventListeners.clear()

    private fun loadData(): Map<String, Any> =
        runCatching {
            storer.load()
                .let(encrypter::decrypt)
                .let(serializer::deserialize)
        }.onFailure { error ->
            eventChannel.offer(SatchelEvent.LoadError(error))
        }.getOrDefault(emptyMap())

    private suspend fun saveData(storage: Map<String, Any>) {
        saveMutex.withLock {
            runCatching {
                serializer.serialize(storage)
                    .let { encrypter.encrypt(it) }
                    .let { storer.save(it) }
            }.onFailure { error ->
                eventChannel.offer(SatchelEvent.SaveError(error))
            }
        }
    }
}
