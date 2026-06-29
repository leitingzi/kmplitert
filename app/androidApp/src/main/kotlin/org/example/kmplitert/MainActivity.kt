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
import org.example.kmplitert.app.core.ResourceUtils

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        ResourceUtils.init(applicationContext)

        CoroutineScope(Dispatchers.Default).launch {
            val modelPath = ResourceUtils.getResourcePath("CelsiusToFahrenheit.tflite")

            val compiler = LiteRTCompiler(filePath = modelPath, accelerator = LiteRTAccelerator.CPU)
            compiler.init()

            val inputBuffers = compiler.getInputBuffers()
            val outputBuffers = compiler.getOutputBuffers()

            inputBuffers[0].writeFloat(floatArrayOf(100f))

            compiler.run(inputBuffers, outputBuffers)

            val result = outputBuffers[0].readFloat()
            println("result: ${result.contentToString()}")

            compiler.close()
        }

        setContent {
            App()
        }
    }
}