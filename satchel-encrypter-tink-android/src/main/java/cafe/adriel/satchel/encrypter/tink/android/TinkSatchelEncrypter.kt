package cafe.adriel.satchel.encrypter.tink.android

import android.content.Context
import android.net.Uri
import cafe.adriel.satchel.encrypter.SatchelEncrypter
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.aead.AesGcmKeyManager
import com.google.crypto.tink.integration.android.AndroidKeysetManager

class TinkSatchelEncrypter private constructor(
    private val aead: Aead,
    private val associatedData: ByteArray
) : SatchelEncrypter {

    companion object {

        private const val DEFAULT_KEYSET_NAME = "satchel.keyset"
        private const val DEFAULT_SHARED_PREF_NAME = "satchel.pref"
        private val DEFAULT_MASTER_KEY_URI = Uri.parse("android-keystore://satchel/keystore")
        private val DEFAULT_KEY_TEMPLATE = AesGcmKeyManager.aes256GcmTemplate()
        private val DEFAULT_ASSOCIATED_DATA = ByteArray(size = 0)

        init {
            AeadConfig.register()
        }

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

        fun with(
            context: Context,
            masterKeyUri: Uri = DEFAULT_MASTER_KEY_URI,
            keysetName: String = DEFAULT_KEYSET_NAME,
            sharedPrefName: String = DEFAULT_SHARED_PREF_NAME,
            associatedData: ByteArray = DEFAULT_ASSOCIATED_DATA
        ): TinkSatchelEncrypter =
            AndroidKeysetManager.Builder()
                .withMasterKeyUri(masterKeyUri.toString())
                .withKeyTemplate(DEFAULT_KEY_TEMPLATE)
                .withSharedPref(context, keysetName, sharedPrefName)
                .build()
                .run { with(keysetHandle, associatedData) }
    }

    override suspend fun encrypt(data: ByteArray): ByteArray =
        aead.encrypt(data, associatedData)

    override fun decrypt(data: ByteArray): ByteArray =
        when {
            data.isEmpty() -> data
            else -> aead.decrypt(data, associatedData)
        }
}
