package org.example.kmplitert

import java.io.File
import java.io.InputStream
import kotlin.io.path.createTempFile

object ResourceUtils {
    fun getInputStream(filePath: String): InputStream {
        return this::class.java.getResourceAsStream("/$filePath")
            ?: throw Exception("Resource not found: $filePath")
    }

    fun resourceToTempFile(filePath: String): File {
        val input = getInputStream(filePath)
        val file = createTempFile().toFile()

        input.use {
            file.outputStream().use(it::copyTo)
        }

        return file
    }
}