@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.leitingzi.kmplitert.core

import com.google.ai.edge.litert.Accelerator
import com.google.ai.edge.litert.CompiledModel
import com.google.ai.edge.litert.Environment
import com.google.ai.edge.litert.TensorBuffer
import com.google.ai.edge.litert.TensorType

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

    actual suspend fun getInputTensorType(inputName: String): LiteRTTensorType {
        val tensorType = compiledModel.getInputTensorType(inputName = inputName)
        return tensorType.toPlatform()
    }

    actual suspend fun getInputBufferRequirements(inputName: String) {
    }

    actual suspend fun getInputBuffers(): List<TFBuffer> {
        val inputBuffers = compiledModel.createInputBuffers()
        return inputBuffers.map { buffer ->
            AndroidTFBuffer(buffer)
        }
    }

    actual suspend fun getOutputTensorType(outputName: String): LiteRTTensorType {
        val tensorType = compiledModel.getOutputTensorType(outputName = outputName)
        return tensorType.toPlatform()
    }

    actual suspend fun getOutputBufferRequirements(outputName: String) {
    }

    actual suspend fun getOutputBuffers(): List<TFBuffer> {
        val outputBuffers = compiledModel.createOutputBuffers()
        return outputBuffers.map { buffer ->
            AndroidTFBuffer(buffer)
        }
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

    private fun TensorType.toPlatform(): LiteRTTensorType {
        return LiteRTTensorType(
            elementType = this.elementType.toPlatform(),
            layout = this.layout?.toPlatform()
        )
    }

    private fun TensorType.ElementType.toPlatform(): LiteRTElementType {
        return when(this) {
            TensorType.ElementType.INT -> LiteRTElementType.INT
            TensorType.ElementType.FLOAT -> LiteRTElementType.FLOAT
            TensorType.ElementType.INT8 -> LiteRTElementType.INT8
            TensorType.ElementType.BOOLEAN -> LiteRTElementType.BOOLEAN
            TensorType.ElementType.INT64 -> LiteRTElementType.INT64
        }
    }

    private fun TensorType.Layout.toPlatform(): LiteRTLayout {
        return LiteRTLayout(this.dimensions, this.strides)
    }
}

