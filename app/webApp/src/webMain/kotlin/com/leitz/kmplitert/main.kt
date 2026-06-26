package com.leitz.kmplitert

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.leitz.kmplitert.compose.App

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport {
        WithFontResourcesLoaded {
            LaunchedEffect(Unit) {
                val compiler = LiteRTCompiler("model/CelsiusToFahrenheitEx.tflite")
                compiler.init()
                val inputs = compiler.getInputBuffers()
                val outputs = compiler.getOutputBuffers()
                inputs[0].writeFloat(floatArrayOf(10f, 20f, 100f))

                compiler.run(inputs, outputs)
                println("result = ${arrayToString(outputs[0].readFloat())}")
                compiler.close()
            }

            App()
        }
    }
}