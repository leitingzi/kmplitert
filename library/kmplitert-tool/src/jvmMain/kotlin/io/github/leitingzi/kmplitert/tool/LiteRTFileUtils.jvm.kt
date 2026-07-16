@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.leitingzi.kmplitert.tool

import java.io.File

actual object LiteRTFileUtils {
    actual fun createFileFromByteArray(data: ByteArray, fileName: String): String {
        val tempDir = System.getProperty("java.io.tmpdir")
        val file = File(tempDir, fileName)

        if (file.exists()) {
            file.delete()
        }

        file.parentFile?.mkdirs()
        file.writeBytes(data)
        return file.absolutePath
    }

    actual suspend fun readAsset(path: String): ByteArray {
        val classLoader = Thread.currentThread().contextClassLoader ?: this::class.java.classLoader
        val inputStream = classLoader.getResourceAsStream(path)
            ?: throw IllegalStateException("Resource not found at path: $path")
        return inputStream.use { it.readAllBytes() }
    }
}
