@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.leitz.kmplitert

import com.leitz.kmplitert.model.LiteRtCompiledModel

actual class LiteRTCompiler actual constructor(val filePath: String) {
    private lateinit var compiledModel: LiteRtCompiledModel

    actual suspend fun init() {
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