package io.github.leitingzi.kmplitert.core

import java.io.File

actual suspend fun loadResourceAsBytes(name: String): ByteArray {
    // Try to load from resources first
    val inputStream = LiteRTCompiler::class.java.classLoader.getResourceAsStream(name)
    if (inputStream != null) {
        return inputStream.readBytes()
    }
    
    // Fallback for local IDE execution where resources might be accessed via file path
    val commonResFile = File("src/commonTest/resources/$name")
    if (commonResFile.exists()) return commonResFile.readBytes()
    
    val jvmResFile = File("src/jvmTest/resources/$name")
    if (jvmResFile.exists()) return jvmResFile.readBytes()
    
    throw IllegalArgumentException("Resource not found: $name")
}

actual fun LiteRTAccelerator.isSupportedOnCurrentPlatform(): Boolean {
    return true
}
