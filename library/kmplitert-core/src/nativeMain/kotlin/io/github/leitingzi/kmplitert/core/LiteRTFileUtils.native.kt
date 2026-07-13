@file:OptIn(ExperimentalForeignApi::class)
@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.leitingzi.kmplitert.core

import kotlinx.cinterop.*
import platform.posix.*

actual object LiteRTFileUtils {
    @OptIn(UnsafeNumber::class)
    actual fun createFileFromByteArray(data: ByteArray, fileName: String): String {
        // Find a temporary directory
        val tempDir = getenv("TMPDIR")?.toKString() 
            ?: getenv("TEMP")?.toKString() 
            ?: getenv("TMP")?.toKString() 
            ?: "/tmp"
            
        val filePath = if (tempDir.endsWith("/") || tempDir.endsWith("\\")) {
            tempDir + fileName
        } else {
            val separator = if (tempDir.contains("\\")) "\\" else "/"
            tempDir + separator + fileName
        }
        
        val file = fopen(filePath, "wb")
            ?: throw Exception("Failed to open file for writing: $filePath")
        
        try {
            if (data.isNotEmpty()) {
                data.usePinned { pinned ->
                    fwrite(pinned.addressOf(0), 1.convert(), data.size.convert(), file)
                }
            }
        } finally {
            fclose(file)
        }
        
        return filePath
    }
}
