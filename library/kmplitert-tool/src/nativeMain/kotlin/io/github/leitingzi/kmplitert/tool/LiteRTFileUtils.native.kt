@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.leitingzi.kmplitert.tool

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.ExperimentalForeignApi
import platform.posix.*

actual object LiteRTFileUtils {
    @OptIn(ExperimentalForeignApi::class)
    actual fun createFileFromByteArray(data: ByteArray, fileName: String): String {
        val file = fopen(fileName, "wb") ?: throw IllegalStateException("Failed to open file $fileName for writing")
        try {
            data.usePinned { pinned ->
                val written = fwrite(pinned.addressOf(0), 1.toULong(), data.size.toULong(), file)
                if (written != data.size.toULong()) {
                    throw IllegalStateException("Failed to write all data to $fileName")
                }
            }
        } finally {
            fclose(file)
        }
        
        // Return absolute path if possible, but for simple native, relative might be all we have
        // unless we use platform specific ways to get absolute path.
        // For now, return the filename as is.
        return fileName
    }
}
