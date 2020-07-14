package cafe.adriel.satchel

sealed class SatchelEvent {

    data class SaveError(val exception: Throwable) : SatchelEvent()

    data class LoadError(val exception: Throwable) : SatchelEvent()

    data class Set(val key: String) : SatchelEvent()

    data class Remove(val key: String) : SatchelEvent()

    object Clear : SatchelEvent()
}
