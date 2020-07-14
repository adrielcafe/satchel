package cafe.adriel.satchel.core.ktx

import java.io.File

val File.isEmpty: Boolean
    get() = exists().not() || length() == 0L
