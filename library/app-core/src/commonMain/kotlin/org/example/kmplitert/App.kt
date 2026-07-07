package org.example.kmplitert

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import io.github.leitingzi.kmplitert.core.LiteRTAccelerator
import io.github.leitingzi.kmplitert.core.LiteRTCompiler

@Composable
fun App() {
    LaunchedEffect(Unit) {
        val modelPath = ComposeResourceUtils.getFilePath("mobilenet_v1.tflite")
        val compiler = LiteRTCompiler(filePath = modelPath, accelerator = LiteRTAccelerator.CPU)
        compiler.init()

        val inputs = compiler.getInputBuffers()
        val outputs = compiler.getOutputBuffers()
        println("inputs = ${inputs.size} | outputs = ${outputs.size}")

        val inputBuffer = inputs[0]
        val outputBuffer = outputs[0]
        println("inputSize = ${inputBuffer.size} | outputSize = ${outputBuffer.size}")

//        inputs[0].writeFloat(floatArrayOf(100f))
//        compiler.run(inputs = inputs, outputs = outputs)
//        val result = outputs[0].readFloat()
//        println("result = ${result.contentToString()}")

        compiler.close()
    }
}
