@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package org.example.kmplitert.app.core

import kmplitert.library.app_core.generated.resources.Res
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.create
import platform.Foundation.writeToFile

actual object ResourceUtils {
    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    actual suspend fun getResourcePath(path: String): String {
        val tempDir = NSTemporaryDirectory()
        val fileName = path.substringAfterLast("/")
        val filePath = tempDir + fileName

        if (NSFileManager.defaultManager.fileExistsAtPath(filePath)) {
            return filePath
        }

        val bytes = Res.readBytes("files/$path")
        val data = bytes.usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
        }
        data.writeToFile(filePath, true)
        
        return filePath
    }
}
