package com.leitz.kmplitert

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport {
        WithFontResourcesLoaded {
            LaunchedEffect(Unit) {
                val compiler = LiteRTCompiler()
                compiler.init("CelsiusToFahrenheit.tflite")
                println("compiler = $compiler")
                val inputs = compiler.getInputBuffers()
                val outputs = compiler.getOutputBuffers()
            }

            App()
        }
    }
}