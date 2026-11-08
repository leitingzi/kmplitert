@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package org.example.kmplitert

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
actual fun rememberModelPathResolver(): suspend (String) -> String {
    val context = LocalContext.current.applicationContext
    return remember(context) {
        { uri ->
            val assetPath = uri.removePrefix("file:///android_asset/")
            require(assetPath != uri && assetPath.isNotBlank()) {
                "Unsupported bundled model URI: $uri"
            }
            withContext(Dispatchers.IO) {
                val modelFile = File.createTempFile("kmplitert-model-", ".tflite", context.cacheDir)
                try {
                    context.assets.open(assetPath).use { input ->
                        modelFile.outputStream().use(input::copyTo)
                    }
                    modelFile.absolutePath
                } catch (error: Exception) {
                    modelFile.delete()
                    throw error
                }
            }
        }
    }
}
