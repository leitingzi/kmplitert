package org.example.kmplitert

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.github.leitingzi.kmplitert.core.LiteRTAccelerator
import io.github.leitingzi.kmplitert.core.LiteRTCompiler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.example.kmplitert.app.core.App
import org.example.kmplitert.app.core.AssetUtils

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        AssetUtils.init(applicationContext)

        val modelFile = AssetUtils.assetToTempFile("CelsiusToFahrenheit.tflite")

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
}