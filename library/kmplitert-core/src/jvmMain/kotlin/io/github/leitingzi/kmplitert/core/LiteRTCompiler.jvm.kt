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

    actual suspend fun getInputTensorType(inputName: String): LiteRTTensorType {
        val model = compiledModel.model ?: throw IllegalStateException("Model is not set")
        val numSignatures = model.getNumSignatures()
        
        for (s in 0 until numSignatures) {
            val signature = model.getSignature(s)
            
            // Try to get by signature input name first
            val numInputs = signature.getNumInputs()
            for (i in 0 until numInputs) {
                if (signature.getInputName(i) == inputName) {
                    return signature.getInputTensor(i).getRankedTensorType().toPlatform()
                }
            }
            
            // Try to get by tensor name
            for (i in 0 until numInputs) {
                val tensor = signature.getInputTensor(i)
                if (tensor.getName() == inputName) {
                    return tensor.getRankedTensorType().toPlatform()
                }
            }
            
            // Try C-API lookup in this signature
            try {
                val tensor = signature.getInputTensor(inputName)
                return tensor.getRankedTensorType().toPlatform()
            } catch (_: Exception) {
            }
        }

        throw IllegalArgumentException("Input tensor $inputName not found in any of the $numSignatures signatures.")
    }

    actual suspend fun getOutputTensorType(outputName: String): LiteRTTensorType {
        val model = compiledModel.model ?: throw IllegalStateException("Model is not set")
        val numSignatures = model.getNumSignatures()

        for (s in 0 until numSignatures) {
            val signature = model.getSignature(s)

            // Try to get by signature output name first
            val numOutputs = signature.getNumOutputs()
            for (i in 0 until numOutputs) {
                if (signature.getOutputName(i) == outputName) {
                    return signature.getOutputTensor(i).getRankedTensorType().toPlatform()
                }
            }

            // Try to get by tensor name
            for (i in 0 until numOutputs) {
                val tensor = signature.getOutputTensor(i)
                if (tensor.getName() == outputName) {
                    return tensor.getRankedTensorType().toPlatform()
                }
            }

            // Try C-API lookup in this signature
            try {
                val tensor = signature.getOutputTensor(outputName)
                return tensor.getRankedTensorType().toPlatform()
            } catch (_: Exception) {
            }
        }
        
        throw IllegalArgumentException("Output tensor $outputName not found in any of the $numSignatures signatures.")
    }

    actual suspend fun getInputBufferRequirements(inputName: String): LiteRTBufferRequirements {
        val model = compiledModel.model ?: throw IllegalStateException("Model is not set")
        val numSignatures = model.getNumSignatures()

        for (s in 0 until numSignatures) {
            val signature = model.getSignature(s)
            val numInputs = signature.getNumInputs()
            for (i in 0 until numInputs) {
                if (signature.getInputName(i) == inputName || signature.getInputTensor(i).getName() == inputName) {
                    val requirements = compiledModel.getInputBufferRequirements(s, i.toLong()).toPlatform()
                    if (requirements.strides.isEmpty()) {
                        val tensorType = signature.getInputTensor(i).getRankedTensorType().toPlatform()
                        return requirements.copy(strides = tensorType.layout?.strides ?: emptyList())
                    }
                    return requirements
                }
            }
        }
        throw IllegalArgumentException("Input tensor $inputName not found")
    }

    actual suspend fun getOutputBufferRequirements(outputName: String): LiteRTBufferRequirements {
        val model = compiledModel.model ?: throw IllegalStateException("Model is not set")
        val numSignatures = model.getNumSignatures()

        for (s in 0 until numSignatures) {
            val signature = model.getSignature(s)
            val numOutputs = signature.getNumOutputs()
            for (i in 0 until numOutputs) {
                if (signature.getOutputName(i) == outputName || signature.getOutputTensor(i.toLong()).getName() == outputName) {
                    val requirements = compiledModel.getOutputBufferRequirements(s, i.toLong()).toPlatform()
                    if (requirements.strides.isEmpty()) {
                        val tensorType = signature.getOutputTensor(i.toLong()).getRankedTensorType().toPlatform()
                        return requirements.copy(strides = tensorType.layout?.strides ?: emptyList())
                    }
                    return requirements
                }
            }
        }
        throw IllegalArgumentException("Output tensor $outputName not found")
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
        if (::compiledModel.isInitialized) {
            compiledModel.destroy()
        }
    }

    private fun LiteRTAccelerator.toJvm(): LiteRtHwAcceleratorSet {
        return when (this) {
            LiteRTAccelerator.CPU -> LiteRtHwAcceleratorSet.CPU
            LiteRTAccelerator.GPU -> LiteRtHwAcceleratorSet.GPU
            LiteRTAccelerator.NPU -> LiteRtHwAcceleratorSet.NPU
        }
    }
}

