@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.leitingzi

import com.google.ai.edge.litert.Accelerator
import com.google.ai.edge.litert.CompiledModel
import com.google.ai.edge.litert.Environment
import com.google.ai.edge.litert.TensorBuffer

actual class LiteRTCompiler actual constructor(
    val filePath: String,
    val accelerator: LiteRTAccelerator
) {
    private lateinit var env: Environment
    private lateinit var compiledModel: CompiledModel

    actual suspend fun init() {
        env = Environment.create()
        val options = CompiledModel.Options(accelerator.toAndroid())
        compiledModel = CompiledModel.create(filePath = filePath, options = options, optionalEnv = env)
    }

    actual suspend fun getInputBuffers(): List<TFBuffer> {
        val inputBuffers = compiledModel.createInputBuffers()
        return inputBuffers.map { AndroidTFBuffer(it) }
    }

    actual suspend fun getOutputBuffers(): List<TFBuffer> {
        val outputBuffers = compiledModel.createOutputBuffers()
        return outputBuffers.map { AndroidTFBuffer(it) }
    }

    actual suspend fun run(inputs: List<TFBuffer>, outputs: List<TFBuffer>) {
        val inputs = inputs.toAndroid()
        val outputs = outputs.toAndroid()
        compiledModel.run(inputs, outputs)
    }

    actual suspend fun close() {
        compiledModel.close()
        env.close()
    }

    private fun TFBuffer.toAndroid(): TensorBuffer {
        return (this as AndroidTFBuffer).buffer
    }

    private fun List<TFBuffer>.toAndroid(): List<TensorBuffer> {
        return map { tFBuffer -> tFBuffer.toAndroid() }
    }

    private fun LiteRTAccelerator.toAndroid(): Accelerator {
        return when (this) {
            LiteRTAccelerator.CPU -> Accelerator.CPU
            LiteRTAccelerator.GPU -> Accelerator.GPU
            LiteRTAccelerator.NPU -> Accelerator.NPU
        }
    }
}