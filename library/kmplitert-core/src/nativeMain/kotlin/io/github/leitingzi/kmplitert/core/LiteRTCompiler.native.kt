@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.leitingzi.kmplitert.core

import io.github.leitingzi.kmplitert.core.model.NativeLiteRtCompiledModel

actual class LiteRTCompiler actual constructor(
    val filePath: String,
    val accelerator: LiteRTAccelerator
) {
    private lateinit var compiledModel: NativeLiteRtCompiledModel

    actual suspend fun init() {
        compiledModel = NativeLiteRtCompiledModel.create(filePath, accelerator)
    }

    actual suspend fun getInputTensorType(inputName: String): LiteRTTensorType {
        return compiledModel.getInputTensorType(0, inputName)
    }

    actual suspend fun getOutputTensorType(outputName: String): LiteRTTensorType {
        return compiledModel.getOutputTensorType(0, outputName)
    }

    actual suspend fun getInputBufferRequirements(inputName: String) {
        // TODO
    }

    actual suspend fun getOutputBufferRequirements(outputName: String) {
        // TODO
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


