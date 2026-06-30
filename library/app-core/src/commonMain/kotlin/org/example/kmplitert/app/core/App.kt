package org.example.kmplitert.app.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import io.github.leitingzi.kmplitert.core.LiteRTAccelerator
import io.github.leitingzi.kmplitert.core.LiteRTCompiler

@Composable
fun App() {
    LaunchedEffect(Unit) {
        val modelPath = ResourceUtils.getResourcePath("CelsiusToFahrenheit.tflite")
        val compiler = LiteRTCompiler(filePath = modelPath, accelerator = LiteRTAccelerator.CPU)
        compiler.init()
        val inputs = compiler.getInputBuffers()
//        inputs[0].writeFloat(floatArrayOf(100f))
        val outputs = compiler.getOutputBuffers()
//        compiler.run(inputs = inputs, outputs = outputs)
//        val result = outputs[0].readFloat()
//        println("result = ${result.contentToString()}")

        compiler.close()
    }
}