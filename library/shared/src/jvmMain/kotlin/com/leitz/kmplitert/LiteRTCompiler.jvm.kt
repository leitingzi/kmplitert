package com.leitz.kmplitert

import com.leitz.kmplitert.model.LiteRtCompiledModel

actual class LiteRTCompiler {
    private lateinit var compiledModel: LiteRtCompiledModel

    actual suspend fun init(filePath: String) {
        compiledModel = LiteRtCompiledModel.create(filePath = filePath)
    }

    actual suspend fun getInputBuffers(): List<TFBuffer> {
        return compiledModel.getInputBuffers()
    }

    actual suspend fun getOutputBuffers(): List<TFBuffer> {
        return compiledModel.getOutputBuffers()
    }

    actual suspend fun run(inputs: List<TFBuffer>, outputs: List<TFBuffer>) {
        compiledModel.run(0, inputs, outputs)
    }

    actual suspend fun close() {
        compiledModel.destroy()
    }
}