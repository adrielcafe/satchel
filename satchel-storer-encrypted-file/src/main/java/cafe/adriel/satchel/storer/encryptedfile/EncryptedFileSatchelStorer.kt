package cafe.adriel.satchel.storer.encryptedfile

import android.content.Context
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.EncryptedFile.FileEncryptionScheme
import androidx.security.crypto.MasterKeys
import cafe.adriel.satchel.core.ktx.isEmpty
import cafe.adriel.satchel.storer.SatchelStorer
import java.io.File

class EncryptedFileSatchelStorer private constructor(
    private val file: File,
    private val encryptedFile: EncryptedFile
) : SatchelStorer {

    companion object {

        private val DEFAULT_KEY_GEN_SPEC = MasterKeys.AES256_GCM_SPEC
        private val DEFAULT_ENCRYPTION_SCHEME = FileEncryptionScheme.AES256_GCM_HKDF_4KB

        fun with(file: File, encryptedFile: EncryptedFile): EncryptedFileSatchelStorer =
            EncryptedFileSatchelStorer(file, encryptedFile)

        fun with(context: Context, file: File): EncryptedFileSatchelStorer =
            with(
                file,
                EncryptedFile
                    .Builder(file, context, MasterKeys.getOrCreate(DEFAULT_KEY_GEN_SPEC), DEFAULT_ENCRYPTION_SCHEME)
                    .build()
            )
    }

    override suspend fun save(data: ByteArray) {
        file.delete()
        encryptedFile.openFileOutput().use { stream ->
            stream.write(data)
        }
    }

    override fun load(): ByteArray =
        when {
            file.isEmpty -> ByteArray(0)
            else -> encryptedFile.openFileInput().use { stream ->
                stream.readBytes()
            }
        }
}
