package cafe.adriel.satchel

import cafe.adriel.satchel.encrypter.SatchelEncrypter
import cafe.adriel.satchel.encrypter.none.NoneSatchelEncrypter
import cafe.adriel.satchel.serializer.SatchelSerializer
import cafe.adriel.satchel.serializer.raw.RawSatchelSerializer
import cafe.adriel.satchel.storer.SatchelStorer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class Satchel private constructor(
    private val storer: SatchelStorer,
    private val serializer: SatchelSerializer,
    private val encrypter: SatchelEncrypter,
    dispatcher: CoroutineDispatcher
) : SatchelConcurrentStorage() {

    companion object {

        lateinit var storage: SatchelStorage
            private set

        val isInitialized: Boolean
            get() = ::storage.isInitialized

        fun init(
            storer: SatchelStorer,
            serializer: SatchelSerializer = RawSatchelSerializer,
            encrypter: SatchelEncrypter = NoneSatchelEncrypter,
            dispatcher: CoroutineDispatcher = Dispatchers.IO
        ) {
            check(isInitialized.not()) { "Satchel has already been initialized" }

            storage = with(storer, serializer, encrypter, dispatcher)
        }

        fun with(
            storer: SatchelStorer,
            serializer: SatchelSerializer = RawSatchelSerializer,
            encrypter: SatchelEncrypter = NoneSatchelEncrypter,
            dispatcher: CoroutineDispatcher = Dispatchers.IO
        ): SatchelStorage =
            Satchel(storer, serializer, encrypter, dispatcher)
    }

    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    private val saveChannel = Channel<Unit>(Channel.CONFLATED)
    private val saveMutex = Mutex()

    private val eventChannel = BroadcastChannel<SatchelEvent>(Channel.BUFFERED)
    private var hasEventListeners = false

    override val isClosed: Boolean
        get() = scope.isActive.not()

    init {
        storage += loadStorage()

        saveChannel
            .consumeAsFlow()
            .map { storage.toMap() }
            .onEach(::saveStorage)
            .launchIn(scope)
    }

    override fun onStorageChanged(event: SatchelEvent) {
        saveChannel.sendBlocking(Unit)

        if (hasEventListeners) {
            eventChannel.sendBlocking(event)
        }
    }

    override fun addListener(
        scope: CoroutineScope,
        dispatcher: CoroutineDispatcher,
        listener: suspend (SatchelEvent) -> Unit
    ) {
        hasEventListeners = true

        eventChannel
            .asFlow()
            .onEach(listener)
            .flowOn(dispatcher)
            .launchIn(scope)
    }

    override fun close() {
        scope.cancel()
    }

    private fun loadStorage(): Map<String, Any> =
        storer.load()
            .let(encrypter::decrypt)
            .let(serializer::deserialize)

    private suspend fun saveStorage(storage: Map<String, Any>) {
        withContext(NonCancellable) {
            saveMutex.withLock {
                runCatching {
                    serializer.serialize(storage)
                        .let { encrypter.encrypt(it) }
                        .let { storer.save(it) }
                }.onFailure { error ->
                    if (hasEventListeners) {
                        eventChannel.sendBlocking(SatchelEvent.SaveError(error))
                    }
                }
            }
        }
    }
}
