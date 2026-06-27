@file:OptIn(ExperimentalComposeUiApi::class)

package org.example.kmplitert

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.leitz.kmplitert.LiteRTAccelerator
import com.leitz.kmplitert.LiteRTCompiler
import com.leitz.kmplitert.arrayToString
import com.leitz.kmplitert.compose.App
import com.leitz.kmplitert.compose.WithFontResourcesLoaded

fun main() = setContent {
    LaunchedEffect(Unit) {
        val compiler = LiteRTCompiler(
            filePath = "model/CelsiusToFahrenheitEx.tflite",
            accelerator = LiteRTAccelerator.NPU
        )
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

fun setContent(content: @Composable () -> Unit) {
    ComposeViewport {
        WithFontResourcesLoaded {
            content()
        }
    }
}