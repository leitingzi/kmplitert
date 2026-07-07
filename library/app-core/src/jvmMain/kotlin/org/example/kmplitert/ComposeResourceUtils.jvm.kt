@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package org.example.kmplitert

import kmplitert.library.app_core.generated.resources.Res
import java.io.File

actual object ComposeResourceUtils {
    actual suspend fun getFilePath(resourcePath: String): String {
        val tempDir = System.getProperty("java.io.tmpdir")
        val file = File(tempDir, "kmplitert_res/$resourcePath")
        if (file.exists()) {
            return file.absolutePath
        }
        val bytes = Res.readBytes("files/$resourcePath")
        file.parentFile?.mkdirs()
        file.writeBytes(bytes)
        return file.absolutePath
    }
}
