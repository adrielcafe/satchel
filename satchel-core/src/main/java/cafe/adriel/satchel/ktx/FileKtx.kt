package cafe.adriel.satchel.ktx

import java.io.File

val File.isEmpty: Boolean
    get() = exists().not() || length() == 0L
