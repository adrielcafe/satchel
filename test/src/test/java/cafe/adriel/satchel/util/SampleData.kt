package cafe.adriel.satchel.util

object SampleData {

    private val sampleDataClass = SampleDataClass(
        string = "Lorem ipsum dolor sit amet",
        boolean = true,
        int = Int.MIN_VALUE,
        long = Long.MIN_VALUE,
        float = Float.MIN_VALUE,
        double = Double.MIN_VALUE
    )

    private val sampleSealedDataClass = SampleSealedClass.DataClass(
        string = "Lorem ipsum dolor sit amet",
        boolean = true,
        int = Int.MIN_VALUE,
        long = Long.MIN_VALUE,
        float = Float.MIN_VALUE,
        double = Double.MIN_VALUE,
        dataClass = sampleDataClass
    )

    private val sampleClass = SampleClass(
        string = "Lorem ipsum dolor sit amet",
        boolean = true,
        int = Int.MIN_VALUE,
        long = Long.MIN_VALUE,
        float = Float.MIN_VALUE,
        double = Double.MIN_VALUE
    )

    val primitives = mapOf(
        "string" to "Lorem ipsum dolor sit amet",
        "boolean" to true,
        "int" to Int.MAX_VALUE,
        "long" to Long.MAX_VALUE,
        "float" to Float.MAX_VALUE,
        "double" to Double.MAX_VALUE
    )

    val listOfPrimitives = mapOf(
        "list string" to listOf("lorem", "ipsum"),
        "list boolean" to listOf(true, false),
        "list int" to listOf(Int.MIN_VALUE, Int.MAX_VALUE),
        "list long" to listOf(Long.MIN_VALUE, Long.MAX_VALUE),
        "list float" to listOf(Float.MIN_VALUE, Float.MAX_VALUE),
        "list double" to listOf(Double.MIN_VALUE, Double.MAX_VALUE)
    )

    val serializableClasses = mapOf(
        "data class" to sampleDataClass,
        "sealed data class" to sampleSealedDataClass,
        "class" to sampleClass
    )

    val listOfSerializableClasses = mapOf(
        "serializable classes" to listOf(sampleDataClass, sampleSealedDataClass, sampleClass)
    )

    val allSupportedTypes = primitives + listOfPrimitives + serializableClasses + listOfSerializableClasses
}
