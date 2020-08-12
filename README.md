[![JitPack](https://img.shields.io/jitpack/v/github/adrielcafe/satchel.svg?style=for-the-badge)](https://jitpack.io/#adrielcafe/satchel) 
[![Android API](https://img.shields.io/badge/api-16%2B-brightgreen.svg?style=for-the-badge)](https://android-arsenal.com/api?level=16) 
[![Github Actions](https://img.shields.io/github/workflow/status/adrielcafe/satchel/main/master?style=for-the-badge)](https://github.com/adrielcafe/satchel/actions) 
[![Codacy](https://img.shields.io/codacy/grade/e072b5e37b094518a7cd672086ac390a.svg?style=for-the-badge)](https://www.codacy.com/app/adriel_cafe/satchel) 
[![Kotlin](https://img.shields.io/github/languages/top/adrielcafe/satchel.svg?style=for-the-badge)](https://kotlinlang.org/) 
[![ktlint](https://img.shields.io/badge/code%20style-%E2%9D%A4-FF4081.svg?style=for-the-badge)](https://ktlint.github.io/) 
[![License MIT](https://img.shields.io/github/license/adrielcafe/satchel.svg?style=for-the-badge&color=yellow)](https://opensource.org/licenses/MIT)

<p align="center">
  <img src="https://github.com/adrielcafe/satchel/blob/master/satchel.png?raw=true">
</p>

### *Satchel* is a powerful and flexible key-value storage with batteries-included for Android and JVM.

It's backed by [Coroutines](https://kotlinlang.org/docs/reference/coroutines-overview.html) and great third-party libraries ([Tink](https://github.com/google/tink), [Kryo](https://github.com/EsotericSoftware/kryo) and [Protobuf](https://github.com/protocolbuffers/protobuf) to name a few).

# Features
* Fast: see the [Benchmark](#benchmark) results
* Small: the [core library](#setup) has ~35kb and contains everything you need to get started
* Simple: has an easy to use [API](#api)
* Modular: 10 (optional) built-in [modules](#modules) to choose from
* Extensible: create your own [Storer](#build-your-own-storer), [Encrypter](#build-your-own-encrypter) and [Serializer](#build-your-own-serializer)

## Supported types
- [x] `Double` and `List<Double>`
- [x] `Float` and `List<Float>`
- [x] `Int` and `List<Int>`
- [x] `Long` and `List<Long>`
- [x] `Boolean` and `List<Boolean>`
- [x] `String` and `List<String>`
- [x] `Serializable`¹

¹ *Not supported by `satchel-serializer-protobuf-lite`*

# Setup
1. Add the JitPack repository to your project level `build.gradle`:
```gradle
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

2. Next, add the desired dependencies to the module `build.gradle`:
```gradle
dependencies {
    // Core (required)
    implementation "com.github.adrielcafe.satchel:satchel-core:$currentVersion"

    // Storers
    implementation "com.github.adrielcafe.satchel:satchel-storer-encrypted-file:$currentVersion"

    // Encrypters
    implementation "com.github.adrielcafe.satchel:satchel-encrypter-cipher:$currentVersion"
    implementation "com.github.adrielcafe.satchel:satchel-encrypter-jose4j:$currentVersion"
    implementation "com.github.adrielcafe.satchel:satchel-encrypter-tink-android:$currentVersion"
    implementation "com.github.adrielcafe.satchel:satchel-encrypter-tink-jvm:$currentVersion"

    // Serializers
    implementation "com.github.adrielcafe.satchel:satchel-serializer-base64-android:$currentVersion"
    implementation "com.github.adrielcafe.satchel:satchel-serializer-base64-jvm:$currentVersion"
    implementation "com.github.adrielcafe.satchel:satchel-serializer-gzip:$currentVersion"
    implementation "com.github.adrielcafe.satchel:satchel-serializer-kryo:$currentVersion"
    implementation "com.github.adrielcafe.satchel:satchel-serializer-protobuf-lite:$currentVersion"
}
```
Current version: [![JitPack](https://img.shields.io/jitpack/v/github/adrielcafe/satchel.svg?style=flat-square)](https://jitpack.io/#adrielcafe/satchel)

# Usage
Take a look at the [sample app](https://github.com/adrielcafe/satchel/tree/master/sample/src/main/java/cafe/adriel/satchel/sample) for a working example.

## Global instance
First initialize Satchel's global instance by calling `Satchel.init()`:
```kotlin
Satchel.init(
    storer = FileSatchelStorer(storageFile),
    encrypter = BypassSatchelEncrypter,
    serializer = RawSatchelSerializer
)
```

Now you can use `Satchel.storage` everywhere:
```kotlin
Satchel.storage["key"] = "value"
```

It's also possible to check if Satchel was already initialized:
```kotlin
if (Satchel.isInitialized.not()) {
    // Init
}
```

## Local instance
Use `Satchel.with()` to create a local instance:
```kotlin
val satchel = Satchel.with(
    storer = FileSatchelStorer(storageFile),
    encrypter = BypassSatchelEncrypter,
    serializer = RawSatchelSerializer
)
```

And start using it:
```kotlin
satchel["key"] = "value"
```

## API
Satchel has a simple and familiar [API](https://github.com/adrielcafe/satchel/blob/master/satchel-core/src/main/java/cafe/adriel/satchel/SatchelStorage.kt) based on [MutableMap](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-mutable-map/) and [SharedPreferences](https://developer.android.com/reference/android/content/SharedPreferences):
```kotlin
satchel.apply {
    val firstName = get<String>("firstName")

    val notificationsEnabled = getOrDefault("notificationsEnabled", false)

    val favoritePostIds = getOrDefault("favoritePostIds") { emptySet<Int>() }

    val registeredAt = getOrSet("registeredAt", currentTimestamp)

    val lastName = getOrSet("lastName") { "Doe" }

    set("username", "john.doe")

    setIfAbsent("lastName", lastName)

    keys.forEach { key ->
        // ...
    }

    when {
        isEmpty -> { /* ... */ }
        size == 1 -> { /* ... */ }
        contains("username") -> { /* ... */ }
    }

    remove("favoritePostIds")

    clear()
}
```

But unlike `SharedPreferences`, there's no `apply()` or `commit()`. Changes are **saved asynchronously** every time a write operation (`set()`, `remove()` and `clear()`) happens.

### Delegates
It's possible to delegate the job of `get` and `set` the value of a specific key:
```kotlin
private var favoritePostIds by satchel.value(key = "favoritePostIds", defaultValue = emptySet<Int>())

// Will call set(key, value)
favoritePostIds = setOf(1, 2, 3)

// Will call getOrDefault(key, defaultValue)
showFavoritePosts(favoritePostIds)
```

If you doesn't specify a default value, it will return a nullable value:
```kotlin
private var username by satchel.value<String>("username")

username?.let(::showProfile)
```

### Events
You can be notified every time the storage changes, just call `addListener()` to register a listener in the specified `CoroutineScope`:
```kotlin
satchel.addListener(lifecycleScope) { event ->
    when (event) {
        is SatchelEvent.Set -> { /* ... */ }
        is SatchelEvent.Remove -> { /* ... */ }
        is SatchelEvent.Clear -> { /* ... */ }
    }
}
```

## Modules
Satchel has 3 different categories of modules:
* **Storers**: responsible for reading and writing to the file system
* **Encrypters**: responsible for encryption and decryption
* **Serializers**: responsible for serialization and deserialization 

The core library comes with one stock module for each category: [FileSatchelStorer](#FileSatchelStorer), [BypassSatchelEncrypter](#BypassSatchelEncrypter) and [RawSatchelSerializer](#RawSatchelSerializer). All the other libraries are *optional*.

### Storers
If you are developing for Android, I recommend to use the [Context.filesDir](https://developer.android.com/training/data-storage/app-specific) as the parent folder. If you want to save in the external storage remember to [ask for write permission](https://developer.android.com/training/data-storage#permissions) first.
```kotlin
val file = File(context.filesDir, "satchel.storage")
```

#### [FileSatchelStorer](https://github.com/adrielcafe/satchel/blob/master/satchel-core/src/main/java/cafe/adriel/satchel/storer/file/FileSatchelStorer.kt)
Uses the `FileOutputStream` and `FileInputStream` to read and write without do any modification.
```kotlin
val storer = FileSatchelStorer(file)
```

#### [EncryptedFileSatchelStorer](https://github.com/adrielcafe/satchel/blob/master/satchel-storer-encrypted-file/src/main/java/cafe/adriel/satchel/storer/encryptedfile/EncryptedFileSatchelStorer.kt)
Uses the `EncryptedFile` from [Jetpack Security](https://developer.android.com/topic/security/data.md) to read/write and also takes care of encryption/decryption.
```kotlin
val storer = EncryptedFileSatchelStorer.with(applicationContext, file)
```

#### Build your own Storer
Create a `class` or `object` that implements the `SatchelStorer` interface: 
```kotlin
object MySatchelStorer : SatchelStorer {
    
    suspend fun store(data: ByteArray) {
        // Save the ByteArray wherever you want
    }

    fun retrieve(): ByteArray {
        // Load and return the stored ByteArray
    }
}
```

### Encrypters
:warning: Satchel doesn't store your crypto keys, it only uses it. So make sure to store them in a safe place.

#### [BypassSatchelEncrypter](https://github.com/adrielcafe/satchel/blob/master/satchel-core/src/main/java/cafe/adriel/satchel/encrypter/bypass/BypassSatchelEncrypter.kt)
Just bypass the encryption/decryption.
```kotlin
val encrypter = BypassSatchelEncrypter
```

#### [CipherSatchelEncrypter](https://github.com/adrielcafe/satchel/blob/master/satchel-encrypter-cipher/src/main/java/cafe/adriel/satchel/encrypter/cipher/CipherSatchelEncrypter.kt)
Uses the [Cipher](https://docs.oracle.com/javase/7/docs/api/javax/crypto/Cipher.html) for encryption/decryption.
```kotlin
val transformation = "AES"
val key = KeyGenerator
    .getInstance(transformation)
    .apply { init(256) }
    .generateKey()
val cipherKey = CipherKey.SecretKey(key)
val encrypter = CipherSatchelEncrypter.with(cipherKey, transformation)
```

#### [Jose4jSatchelEncrypter](https://github.com/adrielcafe/satchel/blob/master/satchel-encrypter-jose4j/src/main/java/cafe/adriel/satchel/encrypter/jose4j/Jose4jSatchelEncrypter.kt)
Uses the [Jose4j](https://bitbucket.org/b_c/jose4j/wiki/Home) library for encryption/decryption.
```kotlin
val jwk = RsaJwkGenerator.generateJwk(2048)
val encrypter = Jose4jSatchelEncrypter.with(jwk)
```

#### [TinkSatchelEncrypter](https://github.com/adrielcafe/satchel/blob/master/satchel-encrypter-tink-jvm/src/main/java/cafe/adriel/satchel/encrypter/tink/jvm/TinkSatchelEncrypter.kt) (JVM)
Uses the [Tink](https://github.com/google/tink) JVM library for encryption/decryption.
```kotlin
val keyset = KeysetHandle.generateNew(AesGcmKeyManager.aes256GcmTemplate())
val encrypter = TinkSatchelEncrypter.with(keyset)
```

#### [TinkSatchelEncrypter](https://github.com/adrielcafe/satchel/blob/master/satchel-encrypter-tink-android/src/main/java/cafe/adriel/satchel/encrypter/tink/android/TinkSatchelEncrypter.kt) (Android)
Uses the [Tink](https://github.com/google/tink) Android library for encryption/decryption.
```kotlin
val encrypter = TinkSatchelEncrypter.with(applicationContext)
```

#### Build your own Encrypter
Create a `class` or `object` that implements the `SatchelEncrypter` interface: 
```kotlin
object MySatchelEncrypter : SatchelEncrypter {
    
    suspend fun encrypt(data: ByteArray): ByteArray {
        // Return a encrypted ByteArray
    }

    fun decrypt(data: ByteArray): ByteArray {
        // Return a decrypted ByteArray
    }
}
```

### Serializers

#### [RawSatchelSerializer](https://github.com/adrielcafe/satchel/blob/master/satchel-core/src/main/java/cafe/adriel/satchel/serializer/raw/RawSatchelSerializer.kt)
Uses the `ObjectOutputStream`/`ObjectInputStream` for serialization/deserialization.
```kotlin
val serializer = RawSatchelSerializer
```

#### [GzipSatchelSerializer](https://github.com/adrielcafe/satchel/blob/master/satchel-serializer-gzip/src/main/java/cafe/adriel/satchel/serializer/gzip/GzipSatchelSerializer.kt)
Uses the `GZIPOutputStream`/`GZIPInputStream` for serialization/deserialization.
```kotlin
val serializer = GzipSatchelSerializer
```

#### [Base64SatchelSerializer](https://github.com/adrielcafe/satchel/blob/master/satchel-serializer-base64-jvm/src/main/java/cafe/adriel/satchel/serializer/base64/jvm/Base64SatchelSerializer.kt) (JVM)
Uses the `Base64` from [Java 8](https://docs.oracle.com/javase/8/docs/api/java/util/Base64.html) for serialization/deserialization.
```kotlin
val serializer = Base64SatchelSerializer
```

#### [Base64SatchelSerializer](https://github.com/adrielcafe/satchel/blob/master/satchel-serializer-base64-android/src/main/java/cafe/adriel/satchel/serializer/base64/android/Base64SatchelSerializer.kt) (Android)
Uses the `Base64` from [Android](https://developer.android.com/reference/android/util/Base64) for serialization/deserialization.
```kotlin
val serializer = Base64SatchelSerializer
```

#### [KryoSatchelSerializer](https://github.com/adrielcafe/satchel/blob/master/satchel-serializer-kryo/src/main/java/cafe/adriel/satchel/serializer/kryo/KryoSatchelSerializer.kt)
Uses the [Kryo](https://github.com/EsotericSoftware/kryo) library for serialization/deserialization.
```kotlin
val serializer = KryoSatchelSerializer
```
:warning: At the moment Kryo 5 only works on Android API 26 and later, [this issue](https://github.com/EsotericSoftware/kryo/issues/691) explains how to make it work in previous versions.

#### [ProtobufLiteSatchelSerializer](https://github.com/adrielcafe/satchel/blob/master/satchel-serializer-protobuf-lite/src/main/java/cafe/adriel/satchel/serializer/protobuf/lite/ProtobufLiteSatchelSerializer.kt)
Uses the [Protocol Buffers Java Lite](https://github.com/protocolbuffers/protobuf/blob/master/java/lite.md) library for serialization/deserialization.
```kotlin
val serializer = ProtobufLiteSatchelSerializer
```
:warning: The current implementation doesn't supports [Serializable](https://docs.oracle.com/javase/7/docs/api/java/io/Serializable.html) objects.

#### Build your own Serializer
Create a `class` or `object` that implements the `SatchelSerializer` interface: 
```kotlin
object MySatchelSerializer : SatchelSerializer {

    override suspend fun serialize(data: Map<String, Any>): ByteArray {
        // Transform the Map into a ByteArray
    }

    override fun deserialize(data: ByteArray): Map<String, Any> {
        // Transform the ByteArray into a Map
    }
}
```

# Benchmark
The following benchmark consists in reading and writing 1k strings on Satchel and similar libraries. Also we compared all modules (storers, encrypters and serializers) individually to help you choose the fastest ones (if performance is a must for you).

You can run the benchmark by yourself, just execute the following command:
```shell script
./gradlew benchmark:connectedCheck
```

The benchmark below was made on a [Samsung Galaxy S20](https://www.gsmarena.com/samsung_galaxy_s20-10081.php).

## Similar libraries
For this benchmark, we use a local Satchel instance with the stock modules (`FileSatchelStorer`, `BypassSatchelEncrypter` and `RawSatchelSerializer`) from the core library.

Keep in mind that by using different modules you can get best or worse performance results (see the modules benchmarks below for a detailed comparison).

|                                           | Read (ns)   | Write (ns)    |
|-------------------------------------------|-------------|---------------|
| **Satchel**                               | **23.054** | **217.000**    |
| [SharedPreferences](https://developer.android.com/reference/android/content/SharedPreferences) | 341.693 | 279.346 |
| [MMKV](https://github.com/Tencent/MMKV)   | 461.807   | 551.308         |
| [Paper](https://github.com/pilgr/Paper)   | 71.388.808  | 427.568.730   |
| [Hawk](https://github.com/orhanobut/hawk) | 18.698.000  | 1.829.687.614 |

## Storers
|                              | Read (ns) | Write (ns) |
|------------------------------|-----------|------------|
| `FileSatchelStorer`          | 55.302    | 47.811     |
| `EncryptedFileSatchelStorer` | 261.962   | 322.577    |

## Encrypters
|                          | Read (ns) | Write (ns) |
|--------------------------|-----------|------------|
| `BypassSatchelEncrypter` | 0         | 0          |
| `CipherSatchelEncrypter` | 189.423   | 202.577    |
| `Jose4jSatchelEncrypter` | 394.654   | 498.538    |
| `TinkSatchelEncrypter`   | 46.439    | 55.134     |

## Serializers
|                                     | Read (ns) | Write (ns) |
|-------------------------------------|-----------|------------|
| `RawSatchelSerializer`              | 652.769   | 1.001.346  |
| `GzipSatchelSerializer`             | 741.230   | 1.425.924  |
| `Base64SatchelSerializer` (Android) | 683.231   | 1.029.077  |
| `Base64SatchelSerializer` (JVM)     | 703.769   | 1.041.000  |
| `KryoSatchelSerializer`             | 209.923   | 170.654    |
| `ProtobufLiteSatchelSerializer`     | 629.116   | 1.319.961  |