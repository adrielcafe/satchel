package cafe.adriel.satchel

import cafe.adriel.satchel.encrypter.none.NoneSatchelEncrypter
import cafe.adriel.satchel.serializer.raw.RawSatchelSerializer
import cafe.adriel.satchel.storer.SatchelStorer
import cafe.adriel.satchel.storer.file.FileSatchelStorer
import io.mockk.coVerifyOrder
import io.mockk.spyk
import io.mockk.verify
import java.io.File
import java.util.UUID
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNull
import strikt.assertions.isTrue

class SatchelTest {

    private lateinit var satchel: SatchelStorage

    private lateinit var storer: SatchelStorer
    private val serializer = spyk<RawSatchelSerializer>()
    private val encrypter = spyk<NoneSatchelEncrypter>()

    private val randomFile: File
        get() = File.createTempFile(UUID.randomUUID().toString(), "")

    private val coroutineScope = TestCoroutineScope()
    private val coroutineDispatcher = TestCoroutineDispatcher()

    @BeforeEach
    fun setup() {
        storer = spyk(FileSatchelStorer(randomFile))

        satchel = spyk(
            Satchel.with(storer, serializer, encrypter, coroutineScope, coroutineDispatcher),
            recordPrivateCalls = true
        )
    }

    @Nested
    inner class LoadStorage {

        @Test
        fun `when init Satchel then load, decrypt and deserialize the stored data`() {
            satchel.clear()

            coVerifyOrder {
                storer.load()
                encrypter.decrypt(any())
                serializer.deserialize(any())
            }
        }
    }

    @Nested
    inner class SaveStorage {

        @Test
        fun `when put a new key then notify change and save storage`() {
            satchel["key"] = "value"

            verify(exactly = 1) {
                satchel invokeNoArgs "onStorageChanged"
                satchel invoke "saveStorage"
            }
        }

        @Test
        fun `when remove an existing key then notify change and save storage`() {
            satchel["key"] = "value"
            satchel.remove("key")

            verify(exactly = 2) {
                satchel invokeNoArgs "onStorageChanged"
                satchel invoke "saveStorage"
            }
        }

        @Test
        fun `when remove an unknown key then don't notify change and save storage`() {
            satchel["key"] = "value"
            satchel.remove("unknown key")

            verify(exactly = 1) {
                satchel invokeNoArgs "onStorageChanged"
                satchel invoke "saveStorage"
            }
        }

        @Test
        fun `when clear storage then notify change and save storage`() {
            satchel.clear()

            verify(exactly = 1) {
                satchel invokeNoArgs "onStorageChanged"
                satchel invoke "saveStorage"
            }
        }

        @Test
        fun `when storage changes then encrypt, serialize and store it`() {
            satchel.clear()

            coVerifyOrder {
                serializer.serialize(any())
                encrypter.encrypt(any())
                storer.save(any())
            }
        }
    }

    @Nested
    inner class StorageApi {

        @Nested
        inner class Keys {

            @Test
            fun `when storage is empty then return empty`() {
                expectThat(satchel.keys).isEmpty()
            }

            @Test
            fun `when storage isn't empty then return all keys`() {
                val keys = setOf("key1", "key2", "key3").apply {
                    forEach { satchel[it] = "value" }
                }

                expectThat(satchel.keys) containsExactlyInAnyOrder keys
            }
        }

        @Nested
        inner class Size {

            @Test
            fun `when storage is empty then return zero`() {
                expectThat(satchel.size) isEqualTo 0
            }

            @Test
            fun `when storage isn't empty then return current size`() {
                repeat(3) { i -> satchel["key $i"] = i }

                expectThat(satchel.size) isEqualTo 3
            }
        }

        @Nested
        inner class Get {

            @Test
            fun `when key exists then return the current value`() {
                satchel["key"] = "value"

                expectThat(satchel.get<String>("key")) isEqualTo "value"
            }

            @Test
            fun `when key exists but expects a different type then return null`() {
                satchel["key"] = "value"

                expectThat(satchel.get<Int>("key")).isNull()
            }

            @Test
            fun `when key doesn't exist then return null`() {
                expectThat(satchel.get<String>("key")).isNull()
            }
        }

        @Nested
        inner class GetOrDefault {

            @Test
            fun `when key exists then return the current value`() {
                satchel["key"] = "value"

                expectThat(satchel.getOrDefault("key", "default value")) isEqualTo "value"
            }

            @Test
            fun `when key doesn't exist then return the default value`() {
                expectThat(satchel.getOrDefault("key", "default value")) isEqualTo "default value"
            }
        }

        @Nested
        inner class GetOrSet {

            @Test
            fun `when key exists then return the current value`() {
                satchel["key"] = "value"

                expectThat(satchel.getOrSet("key", "default value")) isEqualTo "value"

                verify(exactly = 0) { satchel["key"] = "default value" }
            }

            @Test
            fun `when key doesn't exist then return the default value and set it`() {
                expectThat(satchel.getOrSet("key", "default value")) isEqualTo "default value"

                verify(exactly = 1) { satchel["key"] = "default value" }
            }
        }

        @Nested
        inner class Set {

            @Test
            fun `when key exists then replace value`() {
                satchel["key"] = "value"
                satchel["key"] = "new value"

                expectThat(satchel.get<String>("key")) isEqualTo "new value"
            }

            @Test
            fun `when key doesn't exist then set value`() {
                satchel["key"] = "value"

                expectThat(satchel.get<String>("key")) isEqualTo "value"
            }
        }

        @Nested
        inner class SetIfAbsent {

            @Test
            fun `when key exists then don't set value`() {
                satchel["key"] = "value"
                satchel.setIfAbsent("key", "new value")

                expectThat(satchel.get<String>("key")) isEqualTo "value"

                verify(exactly = 0) { satchel["key"] = "new value" }
            }

            @Test
            fun `when key doesn't exist then set value`() {
                satchel.setIfAbsent("key", "new value")

                expectThat(satchel.get<String>("key")) isEqualTo "new value"

                verify(exactly = 1) { satchel["key"] = "new value" }
            }
        }

        @Nested
        inner class IsEmpty {

            @Test
            fun `when storage is empty then return true`() {
                expectThat(satchel.isEmpty).isTrue()
            }

            @Test
            fun `when storage isn't empty then return false`() {
                satchel["key"] = "value"

                expectThat(satchel.isEmpty).isFalse()
            }
        }

        @Nested
        inner class Contains {

            @Test
            fun `when key exists then return true`() {
                satchel["key"] = "value"

                expectThat(satchel.contains("key")).isTrue()
            }

            @Test
            fun `when key doesn't exist then return false`() {
                expectThat(satchel.contains("key")).isFalse()
            }
        }

        @Nested
        inner class Remove {

            @Test
            fun `when key exists then remove value`() {
                satchel["key"] = "value"

                satchel.remove("key")

                expectThat(satchel.contains("key")).isFalse()
            }
        }

        @Nested
        inner class Clear {

            @Test
            fun `when storage isn't empty then remove all values`() {
                repeat(3) { i -> satchel["key $i"] = i }

                satchel.clear()

                expectThat(satchel.size) isEqualTo 0
            }
        }
    }
}
