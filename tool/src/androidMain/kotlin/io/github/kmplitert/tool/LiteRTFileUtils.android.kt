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

    private fun getCacheDir(): File {
        return getContext()?.cacheDir ?: File(System.getProperty("java.io.tmpdir") ?: ".")
    }

    actual fun createFileFromByteArray(data: ByteArray, fileName: String): String {
        val file = File(getCacheDir(), fileName)

        if (file.exists()) {
            file.delete()
        }

        file.parentFile?.mkdirs()
        file.writeBytes(array = data)

        return file.absolutePath
    }
}
