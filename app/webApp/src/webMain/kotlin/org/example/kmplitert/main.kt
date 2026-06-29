@file:OptIn(ExperimentalComposeUiApi::class)

package org.example.kmplitert

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import io.github.leitingzi.kmplitert.core.LiteRTAccelerator
import io.github.leitingzi.kmplitert.core.LiteRTCompiler
import io.github.leitingzi.kmplitert.core.arrayToString
import org.example.kmplitert.app.core.App
import org.example.kmplitert.app.core.ResourceUtils
import org.example.kmplitert.app.core.WithFontResourcesLoaded

fun main() = setContent {
    LaunchedEffect(Unit) {
        val modelPath = ResourceUtils.getResourcePath("CelsiusToFahrenheit.tflite")

        val compiler = LiteRTCompiler(filePath = modelPath, accelerator = LiteRTAccelerator.CPU)
        compiler.init()

        val inputBuffers = compiler.getInputBuffers()
        val outputBuffers = compiler.getOutputBuffers()

        inputBuffers[0].writeFloat(floatArrayOf(100f))

        compiler.run(inputBuffers, outputBuffers)

        val result = outputBuffers[0].readFloat()
        println("result: ${arrayToString(result)}")

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