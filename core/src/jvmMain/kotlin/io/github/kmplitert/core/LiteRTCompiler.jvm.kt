@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.kmplitert.core

import io.github.kmplitert.core.model.LiteRtCompiledModel
import io.github.kmplitert.core.model.LiteRtHwAcceleratorSet

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
            val signature = model.getSignature(index = s)
            
            // Try to get by signature input name first
            val numInputs = signature.getNumInputs()
            for (i in 0 until numInputs) {
                if (signature.getInputName(index = i) == inputName) {
                    return signature.getInputTensor(index = i)
                        .getRankedTensorType()
                        .toPlatform()
                }
            }
            
            // Try to get by tensor name
            for (i in 0 until numInputs) {
                val tensor = signature.getInputTensor(index = i)
                if (tensor.getName() == inputName) {
                    return tensor.getRankedTensorType()
                        .toPlatform()
                }
            }
            
            // Try C-API lookup in this signature
            try {
                val tensor = signature.getInputTensor(name = inputName)
                return tensor.getRankedTensorType().toPlatform()
            } catch (_: Exception) {
            }
        }

        throw Exception("Input tensor $inputName not found in any of the $numSignatures signatures.")
    }

    actual suspend fun getOutputTensorType(outputName: String): LiteRTTensorType {
        val model = compiledModel.model ?: throw IllegalStateException("Model is not set")
        val numSignatures = model.getNumSignatures()

        for (s in 0 until numSignatures) {
            val signature = model.getSignature(index = s)

            // Try to get by signature output name first
            val numOutputs = signature.getNumOutputs()
            for (i in 0 until numOutputs) {
                if (signature.getOutputName(index = i) == outputName) {
                    return signature.getOutputTensor(index = i)
                        .getRankedTensorType()
                        .toPlatform()
                }
            }

            // Try to get by tensor name
            for (i in 0 until numOutputs) {
                val tensor = signature.getOutputTensor(index = i)
                if (tensor.getName() == outputName) {
                    return tensor.getRankedTensorType()
                        .toPlatform()
                }
            }

            // Try C-API lookup in this signature
            try {
                val tensor = signature.getOutputTensor(name = outputName)
                return tensor.getRankedTensorType().toPlatform()
            } catch (_: Exception) {
            }
        }
        
        throw Exception("Output tensor $outputName not found in any of the $numSignatures signatures.")
    }

    actual suspend fun getInputBufferRequirements(inputName: String): LiteRTBufferRequirements {
        val model = compiledModel.model ?: throw IllegalStateException("Model is not set")
        val numSignatures = model.getNumSignatures()

        for (s in 0 until numSignatures) {
            val signature = model.getSignature(s)
            val numInputs = signature.getNumInputs()
            for (i in 0 until numInputs) {
                if (signature.getInputName(index = i) == inputName || signature.getInputTensor(index = i).getName() == inputName) {
                    val requirements = compiledModel.getInputBufferRequirements(
                        signature_index = s,
                        input_index = i
                    ).toPlatform()

                    if (requirements.strides.isEmpty()) {
                        val tensorType = signature.getInputTensor(index = i)
                            .getRankedTensorType()
                            .toPlatform()

                        return requirements.copy(strides = tensorType.layout?.strides ?: emptyList())
                    }
                    return requirements
                }
            }
        }
        throw Exception("Input tensor $inputName not found")
    }

    actual suspend fun getOutputBufferRequirements(outputName: String): LiteRTBufferRequirements {
        val model = compiledModel.model ?: throw IllegalStateException("Model is not set")
        val numSignatures = model.getNumSignatures()

        for (s in 0 until numSignatures) {
            val signature = model.getSignature(s)
            val numOutputs = signature.getNumOutputs()
            for (i in 0 until numOutputs) {
                if (signature.getOutputName(index = i) == outputName || signature.getOutputTensor(index = i).getName() == outputName) {
                    val requirements = compiledModel.getOutputBufferRequirements(
                        signature_index = s,
                        output_index = i
                    ).toPlatform()

                    if (requirements.strides.isEmpty()) {
                        val tensorType = signature.getOutputTensor(index = i)
                            .getRankedTensorType()
                            .toPlatform()

                        return requirements.copy(strides = tensorType.layout?.strides ?: emptyList())
                    }
                    return requirements
                }
            }
        }
        throw Exception("Output tensor $outputName not found")
    }

    actual suspend fun getInputBuffers(signatureIndex: Int): List<TFBuffer> {
        return compiledModel.getInputBuffers(signatureIndex.toLong())
    }

    actual suspend fun getOutputBuffers(signatureIndex: Int): List<TFBuffer> {
        return compiledModel.getOutputBuffers(signatureIndex.toLong())
    }

    actual suspend fun run(inputs: List<TFBuffer>, outputs: List<TFBuffer>, signatureIndex: Int) {
        compiledModel.run(
            signature_index = signatureIndex.toLong(),
            input_buffers = inputs,
            output_buffers = outputs
        )
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

