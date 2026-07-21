package io.github.kmplitert.core

actual suspend fun loadResourceAsBytes(name: String): ByteArray {
    val classLoader = LiteRTCompiler::class.java.classLoader
        ?: throw IllegalStateException("Failed to obtain the ClassLoader for LiteRTCompiler.")

    val inputStream = classLoader.getResourceAsStream(name)
        ?: throw IllegalArgumentException("Resource not found: $name")
    return inputStream.readBytes()
}

actual fun LiteRTAccelerator.isSupportedOnCurrentPlatform(): Boolean {
    val runtime = System.getProperty("java.runtime.name")
    // If we are on a real Android device/emulator, this usually contains "Android"
    // On Host JVM (unit tests), it usually contains "Java(TM) SE" or similar.
    return runtime?.contains("Android", ignoreCase = true) == true
}
