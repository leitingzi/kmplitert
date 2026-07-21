package io.github.kmplitert.tool.classification

import java.io.File

suspend fun loadResourceAsBytes(name: String): ByteArray {
    // Try to load from resources first
    val inputStream = ImageClassifier::class.java.classLoader.getResourceAsStream(name)
    if (inputStream != null) {
        return inputStream.readBytes()
    }
    
    // Fallback for local IDE execution
    val searchPaths = listOf(
        "src/commonTest/resources/$name",
        "src/jvmTest/resources/$name",
        "library/kmplitert-core/src/commonTest/resources/$name",
        "library/kmplitert-core/src/jvmTest/resources/$name"
    )
    
    for (path in searchPaths) {
        val file = File(path)
        if (file.exists()) return file.readBytes()
    }
    
    throw IllegalArgumentException("Resource not found: $name")
}
