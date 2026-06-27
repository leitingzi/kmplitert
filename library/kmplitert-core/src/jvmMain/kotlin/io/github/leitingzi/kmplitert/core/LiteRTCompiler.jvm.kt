@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.leitingzi.kmplitert.core

import io.github.leitingzi.kmplitert.core.model.LiteRtCompiledModel
import io.github.leitingzi.kmplitert.core.model.LiteRtHwAcceleratorSet

actual class LiteRTCompiler actual constructor(
    val filePath: String,
    val accelerator: LiteRTAccelerator
) {
    private lateinit var compiledModel: LiteRtCompiledModel

    actual suspend fun init() {
        compiledModel = LiteRtCompiledModel.create(filePath = filePath, accelerator = accelerator.toJvm())
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

    @Suppress("SameReturnValue")
    private fun LiteRTAccelerator.toJvm(): LiteRtHwAcceleratorSet {
        return when (this) {
            LiteRTAccelerator.CPU -> LiteRtHwAcceleratorSet.CPU
            LiteRTAccelerator.GPU -> LiteRtHwAcceleratorSet.CPU
            LiteRTAccelerator.NPU -> LiteRtHwAcceleratorSet.CPU
        }
    }
}