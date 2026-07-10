@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package io.github.leitingzi.kmplitert.core.model

import io.github.leitingzi.kmplitert.core.LiteRTAccelerator
import io.github.leitingzi.kmplitert.core.LiteRTBufferRequirements
import io.github.leitingzi.kmplitert.core.LiteRTElementType
import io.github.leitingzi.kmplitert.core.LiteRTLayout
import io.github.leitingzi.kmplitert.core.LiteRTTensorType
import io.github.leitingzi.kmplitert.core.NativeTFBuffer
import io.github.leitingzi.kmplitert.core.TFBuffer
import io.github.leitingzi.kmplitert.core.toNative
import kotlinx.cinterop.*
import litert.*

class NativeLiteRtCompiledModel(
    val compiledModel: LiteRtCompiledModel,
    val environment: LiteRtEnvironment,
    val model: LiteRtModel
) {
    fun destroy() {
        LiteRtDestroyCompiledModel(compiledModel)
        LiteRtDestroyModel(model)
        LiteRtDestroyEnvironment(environment)
    }

    fun run(signatureIndex: Long, inputs: List<TFBuffer>, outputs: List<TFBuffer>) {
        memScoped {
            val inputBuffers = allocArray<LiteRtTensorBufferVar>(inputs.size)
            inputs.forEachIndexed { i, buffer ->
                inputBuffers[i] = (buffer as NativeTFBuffer).buffer
            }
            val outputBuffers = allocArray<LiteRtTensorBufferVar>(outputs.size)
            outputs.forEachIndexed { i, buffer ->
                outputBuffers[i] = (buffer as NativeTFBuffer).buffer
            }

            val status = LiteRtRunCompiledModel(
                compiledModel,
                signatureIndex.toULong(),
                inputs.size.toULong(),
                inputBuffers,
                outputs.size.toULong(),
                outputBuffers
            )
            check(status == kLiteRtStatusOk) { "Failed to run compiled model: $status" }
        }
    }

    fun getInputBuffers(signatureIndex: Long = 0): List<TFBuffer> {
        return getBuffers(signatureIndex, true)
    }

    fun getOutputBuffers(signatureIndex: Long = 0): List<TFBuffer> {
        return getBuffers(signatureIndex, false)
    }

    fun getInputTensorType(signatureIndex: Long, name: String): LiteRTTensorType {
        return getTensorType(signatureIndex, name, true)
    }

    fun getOutputTensorType(signatureIndex: Long, name: String): LiteRTTensorType {
        return getTensorType(signatureIndex, name, false)
    }

    fun getInputBufferRequirements(signatureIndex: Long, name: String): LiteRTBufferRequirements {
        return getBufferRequirements(signatureIndex, name, true)
    }

    fun getOutputBufferRequirements(signatureIndex: Long, name: String): LiteRTBufferRequirements {
        return getBufferRequirements(signatureIndex, name, false)
    }

    private fun getBufferRequirements(signatureIndex: Long, name: String, isInput: Boolean): LiteRTBufferRequirements {
        return memScoped {
            val signatureRef = alloc<LiteRtSignatureVar>()
            var status = LiteRtGetModelSignature(model, signatureIndex.toULong(), signatureRef.ptr)
            check(status == kLiteRtStatusOk) { "Failed to get signature: $status" }
            val signature = signatureRef.value!!

            val numTensorsRef = alloc<LiteRtParamIndexVar>()
            status = if (isInput) {
                LiteRtGetNumSignatureInputs(signature, numTensorsRef.ptr)
            } else {
                LiteRtGetNumSignatureOutputs(signature, numTensorsRef.ptr)
            }
            check(status == kLiteRtStatusOk) { "Failed to get num tensors: $status" }
            val numTensors = numTensorsRef.value.toInt()

            var tensorIndex: Int? = null

            // 1. Try to find by signature name
            for (i in 0 until numTensors) {
                val cName = if (isInput) LiteRtGetSignatureInputNameSafe(signature, i) else LiteRtGetSignatureOutputNameSafe(signature, i)
                if (cName?.toKString() == name) {
                    tensorIndex = i
                    break
                }
            }

            // 2. Try to find by tensor name
            if (tensorIndex == null) {
                for (i in 0 until numTensors) {
                    val tensorRef = alloc<LiteRtTensorVar>()
                    status = if (isInput) {
                        LiteRtGetSignatureInputTensorByIndex(signature, i.toULong(), tensorRef.ptr)
                    } else {
                        LiteRtGetSignatureOutputTensorByIndex(signature, i.toULong(), tensorRef.ptr)
                    }
                    if (status == kLiteRtStatusOk) {
                        val cName = LiteRtGetTensorNameSafe(tensorRef.value!!)
                        if (cName?.toKString() == name) {
                            tensorIndex = i
                            break
                        }
                    }
                }
            }

            if (tensorIndex != null) {
                val bufferRequirementsRef = alloc<LiteRtTensorBufferRequirementsVar>()
                status = if (isInput) {
                    LiteRtGetCompiledModelInputBufferRequirements(compiledModel, signatureIndex.toULong(), tensorIndex.toULong(), bufferRequirementsRef.ptr)
                } else {
                    LiteRtGetCompiledModelOutputBufferRequirements(compiledModel, signatureIndex.toULong(), tensorIndex.toULong(), bufferRequirementsRef.ptr)
                }
                check(status == kLiteRtStatusOk) { "Failed to get buffer requirements: $status" }
                val requirements: LiteRTBufferRequirements = bufferRequirementsRef.value!!.toPlatform()
                if (requirements.strides.isEmpty()) {
                    val tensorRef = alloc<LiteRtTensorVar>()
                    status = if (isInput) {
                        LiteRtGetSignatureInputTensorByIndex(signature, tensorIndex.toULong(), tensorRef.ptr)
                    } else {
                        LiteRtGetSignatureOutputTensorByIndex(signature, tensorIndex.toULong(), tensorRef.ptr)
                    }
                    if (status == kLiteRtStatusOk) {
                        val rankedType = alloc<LiteRtRankedTensorType>()
                        status = LiteRtGetRankedTensorType(tensorRef.value!!, rankedType.ptr)
                        if (status == kLiteRtStatusOk) {
                            val platformType = rankedType.toPlatform()
                            return LiteRTBufferRequirements(
                                supportedTypes = requirements.supportedTypes,
                                bufferSize = requirements.bufferSize,
                                strides = platformType.layout?.strides ?: emptyList()
                            )
                        }
                    }
                }
                return requirements
            }
            
            throw IllegalArgumentException("${if (isInput) "Input" else "Output"} tensor $name not found")
        }
    }

    private fun LiteRtTensorBufferRequirements.toPlatform(): LiteRTBufferRequirements {
        val numTypes = LiteRtGetTensorBufferRequirementsNumSupportedTypesSafe(this)
        val supportedTypes = mutableListOf<io.github.leitingzi.kmplitert.core.LiteRTTensorBufferType>()
        for (i in 0 until numTypes) {
            val type = LiteRtGetTensorBufferRequirementsSupportedTypeSafe(this, i)
            supportedTypes.add(type.toPlatform())
        }

        val bufferSize = LiteRtGetTensorBufferRequirementsBufferSizeSafe(this).toInt()

        val numStrides = LiteRtGetTensorBufferRequirementsNumStridesSafe(this)
        val stridesPtr = LiteRtGetTensorBufferRequirementsStridesSafe(this)
        val strides = mutableListOf<Int>()
        if (numStrides > 0 && stridesPtr != null) {
            for (i in 0 until numStrides) {
                strides.add(stridesPtr[i].toInt())
            }
        }

        return LiteRTBufferRequirements(
            supportedTypes = supportedTypes,
            bufferSize = bufferSize,
            strides = strides
        )
    }

    private fun LiteRtTensorBufferType.toPlatform(): io.github.leitingzi.kmplitert.core.LiteRTTensorBufferType {
        return when (this) {
            kLiteRtTensorBufferTypeHostMemory -> io.github.leitingzi.kmplitert.core.LiteRTTensorBufferType.HostMemory
            kLiteRtTensorBufferTypeAhwb -> io.github.leitingzi.kmplitert.core.LiteRTTensorBufferType.Ahwb
            kLiteRtTensorBufferTypeIon -> io.github.leitingzi.kmplitert.core.LiteRTTensorBufferType.Ion
            kLiteRtTensorBufferTypeDmaBuf -> io.github.leitingzi.kmplitert.core.LiteRTTensorBufferType.DmaBuf
            kLiteRtTensorBufferTypeFastRpc -> io.github.leitingzi.kmplitert.core.LiteRTTensorBufferType.FastRpc
            kLiteRtTensorBufferTypeGlBuffer -> io.github.leitingzi.kmplitert.core.LiteRTTensorBufferType.GlBuffer
            kLiteRtTensorBufferTypeGlTexture -> io.github.leitingzi.kmplitert.core.LiteRTTensorBufferType.GlTexture
            kLiteRtTensorBufferTypeOpenClBuffer -> io.github.leitingzi.kmplitert.core.LiteRTTensorBufferType.OpenClBuffer
            kLiteRtTensorBufferTypeOpenClBufferFp16 -> io.github.leitingzi.kmplitert.core.LiteRTTensorBufferType.OpenClBufferFp16
            kLiteRtTensorBufferTypeOpenClTexture -> io.github.leitingzi.kmplitert.core.LiteRTTensorBufferType.OpenClTexture
            kLiteRtTensorBufferTypeOpenClTextureFp16 -> io.github.leitingzi.kmplitert.core.LiteRTTensorBufferType.OpenClTextureFp16
            kLiteRtTensorBufferTypeOpenClBufferPacked -> io.github.leitingzi.kmplitert.core.LiteRTTensorBufferType.OpenClBufferPacked
            kLiteRtTensorBufferTypeOpenClImageBuffer -> io.github.leitingzi.kmplitert.core.LiteRTTensorBufferType.OpenClImageBuffer
            kLiteRtTensorBufferTypeOpenClImageBufferFp16 -> io.github.leitingzi.kmplitert.core.LiteRTTensorBufferType.OpenClImageBufferFp16
            kLiteRtTensorBufferTypeWebGpuBuffer -> io.github.leitingzi.kmplitert.core.LiteRTTensorBufferType.WebGpuBuffer
            kLiteRtTensorBufferTypeWebGpuBufferFp16 -> io.github.leitingzi.kmplitert.core.LiteRTTensorBufferType.WebGpuBufferFp16
            kLiteRtTensorBufferTypeWebGpuTexture -> io.github.leitingzi.kmplitert.core.LiteRTTensorBufferType.WebGpuTexture
            kLiteRtTensorBufferTypeWebGpuTextureFp16 -> io.github.leitingzi.kmplitert.core.LiteRTTensorBufferType.WebGpuTextureFp16
            kLiteRtTensorBufferTypeWebGpuImageBuffer -> io.github.leitingzi.kmplitert.core.LiteRTTensorBufferType.WebGpuImageBuffer
            kLiteRtTensorBufferTypeWebGpuImageBufferFp16 -> io.github.leitingzi.kmplitert.core.LiteRTTensorBufferType.WebGpuImageBufferFp16
            kLiteRtTensorBufferTypeWebGpuBufferPacked -> io.github.leitingzi.kmplitert.core.LiteRTTensorBufferType.WebGpuBufferPacked
            kLiteRtTensorBufferTypeVulkanBuffer -> io.github.leitingzi.kmplitert.core.LiteRTTensorBufferType.VulkanBuffer
            kLiteRtTensorBufferTypeVulkanBufferFp16 -> io.github.leitingzi.kmplitert.core.LiteRTTensorBufferType.VulkanBufferFp16
            kLiteRtTensorBufferTypeVulkanTexture -> io.github.leitingzi.kmplitert.core.LiteRTTensorBufferType.VulkanTexture
            kLiteRtTensorBufferTypeVulkanTextureFp16 -> io.github.leitingzi.kmplitert.core.LiteRTTensorBufferType.VulkanTextureFp16
            kLiteRtTensorBufferTypeVulkanImageBuffer -> io.github.leitingzi.kmplitert.core.LiteRTTensorBufferType.VulkanImageBuffer
            kLiteRtTensorBufferTypeVulkanImageBufferFp16 -> io.github.leitingzi.kmplitert.core.LiteRTTensorBufferType.VulkanImageBufferFp16
            kLiteRtTensorBufferTypeVulkanBufferPacked -> io.github.leitingzi.kmplitert.core.LiteRTTensorBufferType.VulkanBufferPacked
            else -> io.github.leitingzi.kmplitert.core.LiteRTTensorBufferType.Unknown
        }
    }

    private fun getTensorType(signatureIndex: Long, name: String, isInput: Boolean): LiteRTTensorType {
        return memScoped {
            val numSignaturesRef = alloc<LiteRtParamIndexVar>()
            var status = LiteRtGetNumModelSignatures(model, numSignaturesRef.ptr)
            check(status == kLiteRtStatusOk) { "Failed to get num signatures: $status" }
            val numSignatures = numSignaturesRef.value.toLong()

            for (s in 0 until numSignatures) {
                val signatureRef = alloc<LiteRtSignatureVar>()
                status = LiteRtGetModelSignature(model, s.toULong(), signatureRef.ptr)
                if (status != kLiteRtStatusOk) continue
                val signature = signatureRef.value!!

                val numTensorsRef = alloc<LiteRtParamIndexVar>()
                status = if (isInput) {
                    LiteRtGetNumSignatureInputs(signature, numTensorsRef.ptr)
                } else {
                    LiteRtGetNumSignatureOutputs(signature, numTensorsRef.ptr)
                }
                if (status != kLiteRtStatusOk) continue
                val numTensors = numTensorsRef.value.toInt()

                // 1. Try to find by signature name
                for (i in 0 until numTensors) {
                    val cName = if (isInput) LiteRtGetSignatureInputNameSafe(signature, i) else LiteRtGetSignatureOutputNameSafe(signature, i)
                    if (cName?.toKString() == name) {
                        val tensorRef = alloc<LiteRtTensorVar>()
                        status = if (isInput) {
                            LiteRtGetSignatureInputTensorByIndex(signature, i.toULong(), tensorRef.ptr)
                        } else {
                            LiteRtGetSignatureOutputTensorByIndex(signature, i.toULong(), tensorRef.ptr)
                        }
                        if (status == kLiteRtStatusOk) {
                            val rankedType = alloc<LiteRtRankedTensorType>()
                            status = LiteRtGetRankedTensorType(tensorRef.value!!, rankedType.ptr)
                            if (status == kLiteRtStatusOk) return rankedType.toPlatform()
                        }
                    }
                }

                // 2. Try to find by tensor name
                for (i in 0 until numTensors) {
                    val tensorRef = alloc<LiteRtTensorVar>()
                    status = if (isInput) {
                        LiteRtGetSignatureInputTensorByIndex(signature, i.toULong(), tensorRef.ptr)
                    } else {
                        LiteRtGetSignatureOutputTensorByIndex(signature, i.toULong(), tensorRef.ptr)
                    }
                    if (status == kLiteRtStatusOk) {
                        val cName = LiteRtGetTensorNameSafe(tensorRef.value!!)
                        if (cName?.toKString() == name) {
                            val rankedType = alloc<LiteRtRankedTensorType>()
                            status = LiteRtGetRankedTensorType(tensorRef.value!!, rankedType.ptr)
                            if (status == kLiteRtStatusOk) return rankedType.toPlatform()
                        }
                    }
                }

                // 3. Try C-API lookup in this signature
                val tensorRef = alloc<LiteRtTensorVar>()
                status = if (isInput) {
                    LiteRtGetSignatureInputTensor(signature, name, tensorRef.ptr)
                } else {
                    LiteRtGetSignatureOutputTensor(signature, name, tensorRef.ptr)
                }

                if (status == kLiteRtStatusOk) {
                    val rankedType = alloc<LiteRtRankedTensorType>()
                    status = LiteRtGetRankedTensorType(tensorRef.value!!, rankedType.ptr)
                    if (status == kLiteRtStatusOk) return rankedType.toPlatform()
                }
            }
            
            throw IllegalArgumentException("${if (isInput) "Input" else "Output"} tensor $name not found in any of the $numSignatures signatures.")
        }
    }

    private fun LiteRtRankedTensorType.toPlatform(): LiteRTTensorType {
        val platformType = when (this.element_type) {
            kLiteRtElementTypeFloat32 -> LiteRTElementType.FLOAT
            kLiteRtElementTypeInt32 -> LiteRTElementType.INT
            kLiteRtElementTypeInt64 -> LiteRTElementType.INT64
            kLiteRtElementTypeBool -> LiteRTElementType.BOOLEAN
            kLiteRtElementTypeInt8 -> LiteRTElementType.INT8
            else -> LiteRTElementType.FLOAT // Default
        }
        return LiteRTTensorType(
            elementType = platformType,
            layout = this.layout.toPlatform()
        )
    }

    private fun litert.LiteRtLayout.toPlatform(): LiteRTLayout {
        val rank = LiteRtGetLayoutRank(this.ptr)
        val hasStrides = LiteRtGetLayoutHasStrides(this.ptr)
        
        val dims = mutableListOf<Int>()
        for (i in 0 until rank) {
            dims.add(this.dimensions[i])
        }
        
        val strides = if (hasStrides) {
            val list = mutableListOf<Int>()
            for (i in 0 until rank) {
                list.add(this.strides[i].toInt())
            }
            list
        } else {
            LiteRTLayout.calculateDefaultStrides(dims)
        }
        
        return LiteRTLayout(dimensions = dims, strides = strides)
    }

    private fun getBuffers(signatureIndex: Long, isInput: Boolean): List<TFBuffer> {
        return memScoped {
            val signatureRef = alloc<LiteRtSignatureVar>()
            var status = LiteRtGetModelSignature(model, signatureIndex.toULong(), signatureRef.ptr)
            check(status == kLiteRtStatusOk) { "Failed to get signature: $status" }
            val signature = signatureRef.value!!

            val numTensorsRef = alloc<LiteRtParamIndexVar>()
            status = if (isInput) {
                LiteRtGetNumSignatureInputs(signature, numTensorsRef.ptr)
            } else {
                LiteRtGetNumSignatureOutputs(signature, numTensorsRef.ptr)
            }
            check(status == kLiteRtStatusOk) { "Failed to get num tensors: $status" }
            val numTensors = numTensorsRef.value.toLong()

            val buffers = mutableListOf<TFBuffer>()
            for (i in 0 until numTensors) {
                val bufferRequirementsRef = alloc<LiteRtTensorBufferRequirementsVar>()
                status = if (isInput) {
                    LiteRtGetCompiledModelInputBufferRequirements(compiledModel, signatureIndex.toULong(), i.toULong(), bufferRequirementsRef.ptr)
                } else {
                    LiteRtGetCompiledModelOutputBufferRequirements(compiledModel, signatureIndex.toULong(), i.toULong(), bufferRequirementsRef.ptr)
                }
                check(status == kLiteRtStatusOk) { "Failed to get buffer requirements: $status" }

                val tensorRef = alloc<LiteRtTensorVar>()
                status = if (isInput) {
                    LiteRtGetSignatureInputTensorByIndex(signature, i.toULong(), tensorRef.ptr)
                } else {
                    LiteRtGetSignatureOutputTensorByIndex(signature, i.toULong(), tensorRef.ptr)
                }
                check(status == kLiteRtStatusOk) { "Failed to get tensor: $status" }

                val rankedType = alloc<LiteRtRankedTensorType>()
                status = LiteRtGetRankedTensorType(tensorRef.value!!, rankedType.ptr)
                check(status == kLiteRtStatusOk) { "Failed to get ranked tensor type: $status" }

                val bufferRef = alloc<LiteRtTensorBufferVar>()
                status = LiteRtCreateManagedTensorBufferFromRequirements(environment, rankedType.ptr, bufferRequirementsRef.value!!, bufferRef.ptr)
                check(status == kLiteRtStatusOk) { "Failed to create managed tensor buffer: $status" }

                buffers.add(NativeTFBuffer(bufferRef.value!!))
            }
            buffers
        }
    }

    companion object {
        fun create(filePath: String, accelerator: LiteRTAccelerator): NativeLiteRtCompiledModel {
            return memScoped {
                val envRef = alloc<LiteRtEnvironmentVar>()
                val statusEnv = LiteRtCreateEnvironment(0, null, envRef.ptr)
                check(statusEnv == kLiteRtStatusOk) { "Failed to create environment: $statusEnv" }
                val env = envRef.value!!

                val modelRef = alloc<LiteRtModelVar>()
                val statusModel = LiteRtCreateModelFromFile(filePath, modelRef.ptr)
                check(statusModel == kLiteRtStatusOk) { "Failed to create model: $statusModel" }
                val model = modelRef.value!!

                val optionsRef = alloc<LiteRtOptionsVar>()
                val statusOpt = LiteRtCreateOptions(optionsRef.ptr)
                check(statusOpt == kLiteRtStatusOk) { "Failed to create options: $statusOpt" }
                val options = optionsRef.value!!

                val hwAcc = accelerator.toNative()
                LiteRtSetOptionsHardwareAccelerators(options, hwAcc)

                val compiledModelRef = alloc<LiteRtCompiledModelVar>()
                val statusCompiled = LiteRtCreateCompiledModel(env, model, options, compiledModelRef.ptr)
                check(statusCompiled == kLiteRtStatusOk) { "Failed to create compiled model: $statusCompiled" }

                LiteRtDestroyOptions(options)

                NativeLiteRtCompiledModel(compiledModelRef.value!!, env, model)
            }
        }
    }
}
