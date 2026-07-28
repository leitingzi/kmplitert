@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.kmplitert.tool

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.ExperimentalForeignApi
import platform.posix.*

actual object LiteRTFileUtils {
    @OptIn(ExperimentalForeignApi::class)
    actual fun createFileFromByteArray(data: ByteArray, fileName: String): String {
        val fullPath = getWritablePath(fileName)
        val file = fopen(fullPath, "wb") ?: throw IllegalStateException("Failed to open file $fullPath for writing")
        try {
            data.usePinned { pinned ->
                val written = fwrite(pinned.addressOf(0), 1.toULong(), data.size.toULong(), file)
                if (written != data.size.toULong()) {
                    throw IllegalStateException("Failed to write all data to $fullPath")
                }
            }
        } finally {
            fclose(file)
        }
        
        return fullPath
    }
}

internal expect fun getWritablePath(fileName: String): String
