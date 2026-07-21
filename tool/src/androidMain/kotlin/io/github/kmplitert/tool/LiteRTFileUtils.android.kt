@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.kmplitert.tool

import android.annotation.SuppressLint
import android.content.Context
import java.io.File

@SuppressLint("StaticFieldLeak")
actual object LiteRTFileUtils {

    internal lateinit var context: Context

    fun init(context: Context) {
        if (::context.isInitialized) {
            return
        }
        this.context = context.applicationContext
    }

    private fun getContext(): Context? {
        if (!::context.isInitialized) {
            return null
        }
        return this.context.applicationContext
    }

    actual fun createFileFromByteArray(data: ByteArray, fileName: String): String {
        val cacheDir: File = getContext()?.cacheDir 
            ?: File(System.getProperty("java.io.tmpdir") ?: ".")
        val file = File(cacheDir, fileName)

        if (file.exists()) {
            file.delete()
        }

        file.parentFile?.mkdirs()
        file.writeBytes(array = data)
        return file.absolutePath
    }
}
