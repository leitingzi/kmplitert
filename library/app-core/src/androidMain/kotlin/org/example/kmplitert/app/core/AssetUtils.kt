package org.example.kmplitert.app.core

import android.annotation.SuppressLint
import android.content.Context
import java.io.File

@SuppressLint("StaticFieldLeak")
object AssetUtils {

    private lateinit var context: Context

    fun init(context: Context) {
        this.context = context.applicationContext
    }

    fun assetToTempFile(filePath: String): File {
        val file = File(context.cacheDir, filePath)
        if (file.exists()) {
            file.delete()
        }

        context.assets.open(filePath).use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        return file
    }
}