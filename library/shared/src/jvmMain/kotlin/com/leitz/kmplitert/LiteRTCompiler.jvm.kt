package com.leitz.kmplitert

import com.leitz.kmplitert.model.LiteRtCompiledModel

actual class LiteRTCompiler {
    private lateinit var compiledModel: LiteRtCompiledModel

    actual suspend fun init(filePath: String) {
        compiledModel = LiteRtCompiledModel.create(filePath = filePath)
    }

    actual suspend fun getInputBuffers(): List<TFBuffer> {
        TODO("Not yet implemented")
    }

    actual suspend fun getOutputBuffers(): List<TFBuffer> {
        TODO("Not yet implemented")
    }

    actual suspend fun run(inputs: List<TFBuffer>, outputs: List<TFBuffer>) {
    }

    actual suspend fun close() {
        compiledModel.destroy()
    }
}