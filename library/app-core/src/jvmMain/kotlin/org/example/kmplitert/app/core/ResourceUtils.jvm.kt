@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package org.example.kmplitert.app.core

import kmplitert.library.app_core.generated.resources.Res
import java.io.File

actual object ResourceUtils {
    actual suspend fun getResourcePath(path: String): String {
        val tempDir = System.getProperty("java.io.tmpdir")
        val file = File(tempDir, "kmplitert_res/$path")
        if (file.exists()) {
            return file.absolutePath
        }
        val bytes = Res.readBytes("files/$path")
        file.parentFile?.mkdirs()
        file.writeBytes(bytes)
        return file.absolutePath
    }
}
