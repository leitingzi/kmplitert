@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package org.example.kmplitert

import android.annotation.SuppressLint
import android.content.Context
import kmplitert.library.app_core.generated.resources.Res
import java.io.File

@SuppressLint("StaticFieldLeak")
actual object ComposeResourceUtils {

    internal lateinit var context: Context

    fun init(context: Context) {
        if (::context.isInitialized) {
            return
        }
        this.context = context.applicationContext
    }

    fun getContext(): Context {
        if (!::context.isInitialized) {
            throw Exception("[ResourceUtils] Context not initialized")
        }

        return this.context.applicationContext
    }

    actual suspend fun getFilePath(resourcePath: String): String {
        val cacheDir = getContext().cacheDir
        val file = File(cacheDir, resourcePath)
        if (file.exists()) {
            return file.absolutePath
        }
        val bytes = Res.readBytes("files/$resourcePath")
        file.parentFile?.mkdirs()
        file.writeBytes(bytes)
        return file.absolutePath
    }
}
