package org.example.kmplitert

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.leitz.kmplitert.LiteRTAccelerator
import com.leitz.kmplitert.LiteRTCompiler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.example.kmplitert.app.core.App
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val context = applicationContext
        val modelFile = copyAssetToCache(context, "CelsiusToFahrenheit.tflite")

        CoroutineScope(Dispatchers.Default).launch {
            val compiler = LiteRTCompiler(filePath = modelFile.path, accelerator = LiteRTAccelerator.CPU)
            compiler.init()
            val inputBuffers = compiler.getInputBuffers()
            val outputBuffers = compiler.getOutputBuffers()
            inputBuffers[0].writeFloat(floatArrayOf(100f))
            compiler.run(inputBuffers, outputBuffers)
            val result = outputBuffers[0].readFloat()
            println("result: ${result.contentToString()}")
            compiler.close()

            modelFile.delete()
        }

        setContent {
            App()
        }
    }

    private fun copyAssetToCache(context: Context, assetName: String): File {
        val file = File(context.cacheDir, assetName)

        if (!file.exists()) {
            context.assets.open(assetName).use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }

        return file
    }
}