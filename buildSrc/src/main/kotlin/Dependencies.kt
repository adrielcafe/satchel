@file:Suppress("Unused", "MayBeConstant", "MemberVisibilityCanBePrivate")

internal object Version {
    const val GRADLE_ANDROID = "4.0.0"
    const val GRADLE_DETEKT = "1.7.4"
    const val GRADLE_KTLINT = "9.2.1"
    const val GRADLE_VERSIONS = "0.28.0"
    const val GRADLE_MAVEN = "2.1"

    const val KOTLIN = "1.3.72"
    const val COROUTINES = "1.3.7"
    
    // Storers
    const val SECURITY_CRYPTO = "1.0.0-rc02"
    
    // Encrypters
    const val TINK = "1.4.0-rc2"
    const val JOSE4J = "0.7.1"
    
    // Serializers
    const val PROTOBUF = "3.12.2"
    const val FLATBUFFERS = "1.12.0"
    const val KRYO = "5.0.0-RC6"

    // Sample
    const val MULTIDEX = "2.0.1"
    const val APP_COMPAT = "1.1.0"
    const val ACTIVITY = "1.1.0"
    const val LIFECYCLE = "2.2.0"

    // Third-party libraries
    const val MMKV = "1.1.2"
    const val PAPER = "2.7.1"
    const val HAWK = "2.0.1"

    // Testing
    const val TEST_JUNIT = "5.6.2"
    const val TEST_STRIKT = "0.26.1"
    const val TEST_MOCKK = "1.10.0"
    
    // Benchmark
    const val BENCHMARK = "1.0.0"
    const val BENCHMARK_JUNIT = "4.13"
    const val BENCHMARK_ANDROID_JUNIT = "1.1.1"
    const val BENCHMARK_ANDROID_RUNNER = "1.2.0"
}

object ProjectLib {
    const val ANDROID = "com.android.tools.build:gradle:${Version.GRADLE_ANDROID}"
    const val KOTLIN = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Version.KOTLIN}"
    const val DETEKT = "io.gitlab.arturbosch.detekt:detekt-gradle-plugin:${Version.GRADLE_DETEKT}"
    const val KTLINT = "org.jlleitschuh.gradle:ktlint-gradle:${Version.GRADLE_KTLINT}"
    const val VERSIONS = "com.github.ben-manes:gradle-versions-plugin:${Version.GRADLE_VERSIONS}"
    const val MAVEN = "com.github.dcendents:android-maven-gradle-plugin:${Version.GRADLE_MAVEN}"
    const val BENCHMARK = "androidx.benchmark:benchmark-gradle-plugin:${Version.BENCHMARK}"
}

object ModuleLib {
    const val KOTLIN = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Version.KOTLIN}"
    const val COROUTINES_CORE = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Version.COROUTINES}"
    const val COROUTINES_ANDROID = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Version.COROUTINES}"

    // Storers
    const val SECURITY_CRYPTO = "androidx.security:security-crypto:${Version.SECURITY_CRYPTO}"
    
    // Encrypters
    const val TINK_JVM = "com.google.crypto.tink:tink:${Version.TINK}"
    const val TINK_ANDROID = "com.google.crypto.tink:tink-android:${Version.TINK}"
    const val JOSE4J = "org.bitbucket.b_c:jose4j:${Version.JOSE4J}"
    
    // Serializers
    const val PROTOBUF = "com.google.protobuf:protobuf-javalite:${Version.PROTOBUF}"
    const val FLATBUFFERS = "com.google.flatbuffers:flatbuffers-java:${Version.FLATBUFFERS}"
    const val KRYO = "com.esotericsoftware.kryo:kryo5:${Version.KRYO}"
    
    // Sample
    const val MULTIDEX = "androidx.multidex:multidex:${Version.MULTIDEX}"
    const val APP_COMPAT = "androidx.appcompat:appcompat:${Version.APP_COMPAT}"
    const val ACTIVITY = "androidx.activity:activity-ktx:${Version.ACTIVITY}"
    const val LIFECYCLE_RUNTIME = "androidx.lifecycle:lifecycle-runtime-ktx:${Version.LIFECYCLE}"
    const val LIFECYCLE_VIEWMODEL = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Version.LIFECYCLE}"

    // Third-party libraries
    const val MMKV = "com.tencent:mmkv-static:${Version.MMKV}"
    const val PAPER = "io.paperdb:paperdb:${Version.PAPER}"
    const val HAWK = "com.orhanobut:hawk:${Version.HAWK}"
}

object TestLib {
    const val JUNIT_API = "org.junit.jupiter:junit-jupiter-api:${Version.TEST_JUNIT}"
    const val JUNIT_ENGINE = "org.junit.jupiter:junit-jupiter-engine:${Version.TEST_JUNIT}"
    const val STRIKT = "io.strikt:strikt-core:${Version.TEST_STRIKT}"
    const val MOCKK = "io.mockk:mockk:${Version.TEST_MOCKK}"
    const val COROUTINES = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Version.COROUTINES}"
}

object BenchmarkLib {
    const val BENCHMARK = "androidx.benchmark:benchmark-junit4:${Version.BENCHMARK}"
    const val JUNIT = "junit:junit:${Version.BENCHMARK_JUNIT}"
    const val ANDROID_JUNIT = "androidx.test.ext:junit-ktx:${Version.BENCHMARK_ANDROID_JUNIT}"
    const val ANDROID_RUNNER = "androidx.test:runner:${Version.BENCHMARK_ANDROID_RUNNER}"
    const val COROUTINES = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Version.COROUTINES}"
}
