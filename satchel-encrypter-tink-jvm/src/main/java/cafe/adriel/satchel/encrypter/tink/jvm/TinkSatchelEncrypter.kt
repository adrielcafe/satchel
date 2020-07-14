package cafe.adriel.satchel.encrypter.tink.jvm

import cafe.adriel.satchel.encrypter.SatchelEncrypter
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.aead.AeadConfig

class TinkSatchelEncrypter private constructor(
    private val aead: Aead,
    private val associatedData: ByteArray
) : SatchelEncrypter {

    companion object {

        private val DEFAULT_ASSOCIATED_DATA = ByteArray(size = 0)

        fun with(
            aead: Aead,
            associatedData: ByteArray = DEFAULT_ASSOCIATED_DATA
        ): TinkSatchelEncrypter =
            TinkSatchelEncrypter(aead, associatedData)

        fun with(
            keysetHandle: KeysetHandle,
            associatedData: ByteArray = DEFAULT_ASSOCIATED_DATA
        ): TinkSatchelEncrypter =
            with(keysetHandle.getPrimitive(Aead::class.java), associatedData)
    }

    init {
        AeadConfig.register()
    }

    override suspend fun encrypt(data: ByteArray): ByteArray =
        aead.encrypt(data, associatedData)

    override fun decrypt(data: ByteArray): ByteArray =
        when {
            data.isEmpty() -> data
            else -> aead.decrypt(data, associatedData)
        }
}
