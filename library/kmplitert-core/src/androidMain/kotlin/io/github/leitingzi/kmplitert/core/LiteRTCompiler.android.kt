@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.leitingzi.kmplitert.core

import com.google.ai.edge.litert.Accelerator
import com.google.ai.edge.litert.CompiledModel
import com.google.ai.edge.litert.TensorBuffer
import com.google.ai.edge.litert.TensorBufferRequirements
import com.google.ai.edge.litert.TensorBufferType
import com.google.ai.edge.litert.TensorType

actual class LiteRTCompiler actual constructor(
    val filePath: String,
    val accelerator: LiteRTAccelerator
) {
    private lateinit var compiledModel: CompiledModel

    actual suspend fun init() {
        val options = CompiledModel.Options(accelerator.toAndroid())
        compiledModel = CompiledModel.create(filePath = filePath, options = options)
    }

    actual suspend fun getInputTensorType(inputName: String): LiteRTTensorType {
        val tensorType = compiledModel.getInputTensorType(inputName = inputName)
        return tensorType.toPlatform()
    }

    actual suspend fun getInputBufferRequirements(inputName: String): LiteRTBufferRequirements {
        val requirements = compiledModel.getInputBufferRequirements(
            inputName = inputName
        ).toPlatform()

        if (requirements.strides.isEmpty()) {
            val tensorType = compiledModel.getInputTensorType(inputName = inputName)
            return requirements.copy(strides = LiteRTLayout.calculateDefaultStrides(
                dimensions = tensorType.layout?.dimensions ?: emptyList())
            )
        }
        return requirements
    }

    actual suspend fun getInputBuffers(signatureIndex: Int): List<TFBuffer> {
        val inputBuffers = compiledModel.createInputBuffers(
            signatureIndex = signatureIndex
        )
        return inputBuffers.map { buffer ->
            AndroidTFBuffer(buffer = buffer)
        }
    }

    actual suspend fun getOutputTensorType(outputName: String): LiteRTTensorType {
        val tensorType = compiledModel.getOutputTensorType(outputName = outputName)
        return tensorType.toPlatform()
    }

    actual suspend fun getOutputBufferRequirements(outputName: String): LiteRTBufferRequirements {
        val requirements = compiledModel.getOutputBufferRequirements(
            outputName = outputName
        ).toPlatform()

        if (requirements.strides.isEmpty()) {
            val tensorType = compiledModel.getOutputTensorType(outputName = outputName)
            return requirements.copy(strides = LiteRTLayout.calculateDefaultStrides(
                dimensions = tensorType.layout?.dimensions ?: emptyList())
            )
        }
        return requirements
    }

    actual suspend fun getOutputBuffers(signatureIndex: Int): List<TFBuffer> {
        val outputBuffers = compiledModel.createOutputBuffers(
            signatureIndex = signatureIndex
        )
        return outputBuffers.map { buffer ->
            AndroidTFBuffer(buffer)
        }
    }

    actual suspend fun run(inputs: List<TFBuffer>, outputs: List<TFBuffer>, signatureIndex: Int) {
        compiledModel.run(
            inputs = inputs.toAndroid(),
            outputs = outputs.toAndroid(),
            signatureIndex = signatureIndex
        )
    }

    actual suspend fun close() {
        if (::compiledModel.isInitialized) {
            compiledModel.close()
        }
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
        val platformStrides = this.strides.ifEmpty {
            LiteRTLayout.calculateDefaultStrides(dimensions = this.dimensions)
        }
        return LiteRTLayout(dimensions = this.dimensions, strides = platformStrides)
    }

    private fun TensorBufferType.toPlatform(): LiteRTTensorBufferType {
        return when(this) {
            TensorBufferType.Unknown -> LiteRTTensorBufferType.Unknown
            TensorBufferType.HostMemory -> LiteRTTensorBufferType.HostMemory
            TensorBufferType.Ahwb -> LiteRTTensorBufferType.Ahwb
            TensorBufferType.Ion -> LiteRTTensorBufferType.Ion
            TensorBufferType.DmaBuf -> LiteRTTensorBufferType.DmaBuf
            TensorBufferType.FastRpc -> LiteRTTensorBufferType.FastRpc
            TensorBufferType.GlBuffer -> LiteRTTensorBufferType.GlBuffer
            TensorBufferType.GlTexture -> LiteRTTensorBufferType.GlTexture
            TensorBufferType.OpenClBuffer -> LiteRTTensorBufferType.OpenClBuffer
            TensorBufferType.OpenClBufferFp16 -> LiteRTTensorBufferType.OpenClBufferFp16
            TensorBufferType.OpenClTexture -> LiteRTTensorBufferType.OpenClTexture
            TensorBufferType.OpenClTextureFp16 -> LiteRTTensorBufferType.OpenClTextureFp16
            TensorBufferType.OpenClBufferPacked -> LiteRTTensorBufferType.OpenClBufferPacked
            TensorBufferType.OpenClImageBuffer -> LiteRTTensorBufferType.OpenClImageBuffer
            TensorBufferType.OpenClImageBufferFp16 -> LiteRTTensorBufferType.OpenClImageBufferFp16
            TensorBufferType.WebGpuBuffer -> LiteRTTensorBufferType.WebGpuBuffer
            TensorBufferType.WebGpuBufferFp16 -> LiteRTTensorBufferType.WebGpuBufferFp16
            TensorBufferType.WebGpuTexture -> LiteRTTensorBufferType.WebGpuTexture
            TensorBufferType.WebGpuTextureFp16 -> LiteRTTensorBufferType.WebGpuTextureFp16
            TensorBufferType.WebGpuImageBuffer -> LiteRTTensorBufferType.WebGpuImageBuffer
            TensorBufferType.WebGpuImageBufferFp16 -> LiteRTTensorBufferType.WebGpuImageBufferFp16
            TensorBufferType.WebGpuBufferPacked -> LiteRTTensorBufferType.WebGpuBufferPacked
            TensorBufferType.VulkanBuffer -> LiteRTTensorBufferType.VulkanBuffer
            TensorBufferType.VulkanBufferFp16 -> LiteRTTensorBufferType.VulkanBufferFp16
            TensorBufferType.VulkanTexture -> LiteRTTensorBufferType.VulkanTexture
            TensorBufferType.VulkanTextureFp16 -> LiteRTTensorBufferType.VulkanTextureFp16
            TensorBufferType.VulkanImageBuffer -> LiteRTTensorBufferType.VulkanImageBuffer
            TensorBufferType.VulkanImageBufferFp16 -> LiteRTTensorBufferType.VulkanImageBufferFp16
            TensorBufferType.VulkanBufferPacked -> LiteRTTensorBufferType.VulkanBufferPacked
        }
    }

    private fun List<TensorBufferType>.toPlatform(): List<LiteRTTensorBufferType> {
        return map { it.toPlatform() }
    }

    private fun TensorBufferRequirements.toPlatform(): LiteRTBufferRequirements {
        return LiteRTBufferRequirements(
            supportedTypes = this.supportedTypes.toPlatform(),
            bufferSize = this.bufferSize,
            strides = this.strides
        )
    }
}

