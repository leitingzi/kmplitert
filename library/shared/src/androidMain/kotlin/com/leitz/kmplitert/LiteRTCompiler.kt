package com.leitz.kmplitert

import com.google.ai.edge.litert.CompiledModel
import com.google.ai.edge.litert.Environment

actual class LiteRTCompiler {
    lateinit var env: Environment
    lateinit var model: CompiledModel

    actual suspend fun init(filePath: String) {
        env = Environment.create()
        model = CompiledModel.create(filePath = filePath, optionalEnv = env)
    }

    actual suspend fun getInputBuffers(): List<TFBuffer> {
        val inputBuffers = model.createInputBuffers()
        return inputBuffers.map { AndroidTFBuffer(it) }
    }

    actual suspend fun getOutputBuffers(): List<TFBuffer> {
        val outputBuffers = model.createOutputBuffers()
        return outputBuffers.map { AndroidTFBuffer(it) }
    }

    actual suspend fun run(inputs: List<TFBuffer>, outputs: List<TFBuffer>) {
        val inputs = inputs.toAndroid()
        val outputs = outputs.toAndroid()
        model.run(inputs, outputs)
    }

    actual suspend fun close() {
        model.close()
        env.close()
    }
}