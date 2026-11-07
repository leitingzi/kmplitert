@file:OptIn(ExperimentalForeignApi::class)
@file:Suppress("DIFFERENT_BIT_WIDTHS")

package io.github.kmplitert.core.model

import io.github.kmplitert.core.LiteRTAccelerator
import io.github.kmplitert.core.LiteRTBufferRequirements
import io.github.kmplitert.core.LiteRTElementType
import io.github.kmplitert.core.LiteRTLayout
import io.github.kmplitert.core.LiteRTTensorBufferType
import io.github.kmplitert.core.LiteRTTensorType
import io.github.kmplitert.core.NativeTFBuffer
import io.github.kmplitert.core.TFBuffer
import io.github.kmplitert.core.toNative
import kotlinx.cinterop.*
import kotlinx.cinterop.get
import litert.*

class NativeLiteRtCompiledModel(
    val compiledModel: LiteRtCompiledModel,
    val environment: LiteRtEnvironment,
    val model: LiteRtModel
) {
    fun destroy() {
        LiteRtDestroyCompiledModel(compiled_model = compiledModel)
        LiteRtDestroyModel(model = model)
        LiteRtDestroyEnvironment(environment = environment)
    }

    fun run(signatureIndex: Int, inputs: List<TFBuffer>, outputs: List<TFBuffer>) {
        memScoped {
            val inputBuffers = allocArray<LiteRtTensorBufferVar>(length = inputs.size)
            inputs.forEachIndexed { i, buffer ->
                inputBuffers[i] = (buffer as NativeTFBuffer).buffer
            }

            val outputBuffers = allocArray<LiteRtTensorBufferVar>(length = outputs.size)
            outputs.forEachIndexed { i, buffer ->
                outputBuffers[i] = (buffer as NativeTFBuffer).buffer
            }

            val status = LiteRtRunCompiledModel(
                compiled_model = compiledModel,
                signature_index = signatureIndex.toULong(),
                num_input_buffers = inputs.size.toULong(),
                input_buffers = inputBuffers,
                num_output_buffers = outputs.size.toULong(),
                output_buffers = outputBuffers
            )
            check(value = status == kLiteRtStatusOk) {
                "Failed to run compiled model: $status"
            }
        }
    }

    fun getInputBuffers(signatureIndex: Int = 0): List<TFBuffer> {
        return getBuffers(signatureIndex = signatureIndex, isInput = true)
    }

    fun getOutputBuffers(signatureIndex: Int = 0): List<TFBuffer> {
        return getBuffers(signatureIndex = signatureIndex, isInput = false)
    }

    fun getInputTensorType(signatureIndex: Int, name: String): LiteRTTensorType {
        return getTensorType(signatureIndex = signatureIndex, name = name, isInput = true)
    }

    fun getOutputTensorType(signatureIndex: Int, name: String): LiteRTTensorType {
        return getTensorType(signatureIndex = signatureIndex, name = name, isInput = false)
    }

    fun getInputBufferRequirements(signatureIndex: Int, name: String): LiteRTBufferRequirements {
        return getBufferRequirements(signatureIndex = signatureIndex, name = name, isInput = true)
    }

    fun getOutputBufferRequirements(signatureIndex: Int, name: String): LiteRTBufferRequirements {
        return getBufferRequirements(signatureIndex = signatureIndex, name = name, isInput = false)
    }

    private fun getBufferRequirements(signatureIndex: Int, name: String, isInput: Boolean): LiteRTBufferRequirements {
        memScoped {
            val signatureRef = alloc<LiteRtSignatureVar>()
            var status = LiteRtGetModelSignature(
                model = model,
                signature_index = signatureIndex.toULong(),
                signature = signatureRef.ptr
            )
            check(value = status == kLiteRtStatusOk) {
                "Failed to get signature: $status"
            }

            val signature = signatureRef.value!!
            val numTensorsRef = alloc<LiteRtParamIndexVar>()
            status = if (isInput) {
                LiteRtGetNumSignatureInputs(signature = signature, num_inputs = numTensorsRef.ptr)
            } else {
                LiteRtGetNumSignatureOutputs(signature = signature, num_outputs = numTensorsRef.ptr)
            }
            check(value = status == kLiteRtStatusOk) {
                "Failed to get num tensors: $status"
            }

            val numTensors = numTensorsRef.value.toInt()
            var tensorIndex: Int? = null

            // 1. Try to find by signature name
            for (i in 0 until numTensors) {
                val cName = getSignatureTensorName(signature, i, isInput)
                if (cName == name) {
                    tensorIndex = i
                    break
                }
            }

            // 2. Try to find by tensor name
            if (tensorIndex == null) {
                for (i in 0 until numTensors) {
                    val tensorRef = alloc<LiteRtTensorVar>()
                    status = if (isInput) {
                        LiteRtGetSignatureInputTensorByIndex(
                            signature = signature,
                            input_idx = i.toULong(),
                            tensor = tensorRef.ptr
                        )
                    } else {
                        LiteRtGetSignatureOutputTensorByIndex(
                            signature = signature,
                            output_idx = i.toULong(),
                            tensor = tensorRef.ptr
                        )
                    }
                    if (status == kLiteRtStatusOk) {
                        val cName = getTensorName(tensorRef.value!!)
                        if (cName == name) {
                            tensorIndex = i
                            break
                        }
                    }
                }
            }

            if (tensorIndex != null) {
                val bufferRequirementsRef = alloc<LiteRtTensorBufferRequirementsVar>()
                status = if (isInput) {
                    LiteRtGetCompiledModelInputBufferRequirements(
                        compiled_model = compiledModel,
                        signature_index = signatureIndex.toULong(),
                        input_index = tensorIndex.toULong(),
                        buffer_requirements = bufferRequirementsRef.ptr
                    )
                } else {
                    LiteRtGetCompiledModelOutputBufferRequirements(
                        compiled_model = compiledModel,
                        signature_index = signatureIndex.toULong(),
                        output_index = tensorIndex.toULong(),
                        buffer_requirements = bufferRequirementsRef.ptr
                    )
                }
                check(value = status == kLiteRtStatusOk) {
                    "Failed to get buffer requirements: $status"
                }

                val requirements = bufferRequirementsRef.value!!.toPlatform()
                if (requirements.strides.isEmpty()) {
                    val tensorRef = alloc<LiteRtTensorVar>()
                    status = if (isInput) {
                        LiteRtGetSignatureInputTensorByIndex(
                            signature = signature,
                            input_idx = tensorIndex.toULong(),
                            tensor = tensorRef.ptr
                        )
                    } else {
                        LiteRtGetSignatureOutputTensorByIndex(
                            signature = signature,
                            output_idx = tensorIndex.toULong(),
                            tensor = tensorRef.ptr
                        )
                    }
                    if (status == kLiteRtStatusOk) {
                        val rankedType = alloc<LiteRtRankedTensorType>()
                        status = LiteRtGetRankedTensorType(
                            tensor = tensorRef.value!!,
                            ranked_tensor_type = rankedType.ptr
                        )
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

            throw Exception(message = "${if (isInput) "Input" else "Output"} tensor $name not found")
        }
    }

    private fun LiteRtTensorBufferRequirements.toPlatform(): LiteRTBufferRequirements {
        return memScoped {
            val numTypesRef = alloc<IntVar>()
            var status = LiteRtGetNumTensorBufferRequirementsSupportedBufferTypes(this@toPlatform, numTypesRef.ptr)
            check(status == kLiteRtStatusOk) { "Failed to get supported buffer type count: $status" }

            val supportedTypes = List(numTypesRef.value) { index ->
                val typeRef = alloc<LiteRtTensorBufferTypeVar>()
                status = LiteRtGetTensorBufferRequirementsSupportedTensorBufferType(
                    this@toPlatform,
                    index,
                    typeRef.ptr
                )
                check(status == kLiteRtStatusOk) { "Failed to get supported buffer type at index $index: $status" }
                typeRef.value.toPlatform()
            }

            val bufferSizeRef = alloc<ULongVar>()
            status = LiteRtGetTensorBufferRequirementsBufferSize(this@toPlatform, bufferSizeRef.ptr)
            check(status == kLiteRtStatusOk) { "Failed to get required buffer size: $status" }

            val numStridesRef = alloc<IntVar>()
            val stridesRef = alloc<CPointerVar<UIntVar>>()
            status = LiteRtGetTensorBufferRequirementsStrides(
                this@toPlatform,
                numStridesRef.ptr,
                stridesRef.ptr
            )
            check(status == kLiteRtStatusOk) { "Failed to get buffer strides: $status" }
            val strideCount = numStridesRef.value
            val strides = if (strideCount == 0) {
                emptyList()
            } else {
                val stridePointer = checkNotNull(stridesRef.value) {
                    "LiteRT returned a null strides pointer for $strideCount strides"
                }
                List(strideCount) { index ->
                    stridePointer[index].toInt()
                }
            }

            LiteRTBufferRequirements(
                supportedTypes = supportedTypes,
                bufferSize = bufferSizeRef.value.toInt(),
                strides = strides
            )
        }
    }

    private fun LiteRtTensorBufferType.toPlatform(): LiteRTTensorBufferType {
        return LiteRTTensorBufferType.from(this.toInt())
    }

    private fun getTensorType(signatureIndex: Int, name: String, isInput: Boolean): LiteRTTensorType {
        memScoped {
            val signatureRef = alloc<LiteRtSignatureVar>()
            var status = LiteRtGetModelSignature(
                model = model,
                signature_index = signatureIndex.toULong(),
                signature = signatureRef.ptr
            )
            check(value = status == kLiteRtStatusOk) {
                "Failed to get signature at index $signatureIndex: $status"
            }

            val signature = signatureRef.value!!
            val numTensorsRef = alloc<LiteRtParamIndexVar>()
            status = if (isInput) {
                LiteRtGetNumSignatureInputs(signature = signature, num_inputs = numTensorsRef.ptr)
            } else {
                LiteRtGetNumSignatureOutputs(signature = signature, num_outputs = numTensorsRef.ptr)
            }
            check(value = status == kLiteRtStatusOk) {
                "Failed to get num tensors: $status"
            }

            val numTensors = numTensorsRef.value.toInt()

            // 1. Try to find by signature name
            for (i in 0 until numTensors) {
                val cName = getSignatureTensorName(signature, i, isInput)

                if (cName == name) {
                    val tensorRef = alloc<LiteRtTensorVar>()
                    status = if (isInput) {
                        LiteRtGetSignatureInputTensorByIndex(
                            signature = signature,
                            input_idx = i.toULong(),
                            tensor = tensorRef.ptr
                        )
                    } else {
                        LiteRtGetSignatureOutputTensorByIndex(
                            signature = signature,
                            output_idx = i.toULong(),
                            tensor = tensorRef.ptr
                        )
                    }
                    if (status == kLiteRtStatusOk) {
                        val rankedType = alloc<LiteRtRankedTensorType>()
                        status = LiteRtGetRankedTensorType(
                            tensor = tensorRef.value!!,
                            ranked_tensor_type = rankedType.ptr
                        )
                        if (status == kLiteRtStatusOk) {
                            return rankedType.toPlatform()
                        }
                    }
                }
            }

            // 2. Try to find by tensor name
            for (i in 0 until numTensors) {
                val tensorRef = alloc<LiteRtTensorVar>()
                status = if (isInput) {
                    LiteRtGetSignatureInputTensorByIndex(
                        signature = signature,
                        input_idx = i.toULong(),
                        tensor = tensorRef.ptr
                    )
                } else {
                    LiteRtGetSignatureOutputTensorByIndex(
                        signature = signature,
                        output_idx = i.toULong(),
                        tensor = tensorRef.ptr
                    )
                }
                if (status == kLiteRtStatusOk) {
                    val cName = getTensorName(tensorRef.value!!)
                    if (cName == name) {
                        val rankedType = alloc<LiteRtRankedTensorType>()
                        status = LiteRtGetRankedTensorType(
                            tensor = tensorRef.value!!,
                            ranked_tensor_type = rankedType.ptr
                        )
                        if (status == kLiteRtStatusOk) {
                            return rankedType.toPlatform()
                        }
                    }
                }
            }

            // 3. Try C-API direct lookup in this signature
            val tensorRef = alloc<LiteRtTensorVar>()
            status = if (isInput) {
                LiteRtGetSignatureInputTensor(
                    signature = signature,
                    input_name = name,
                    tensor = tensorRef.ptr
                )
            } else {
                LiteRtGetSignatureOutputTensor(
                    signature = signature,
                    output_name = name,
                    tensor = tensorRef.ptr
                )
            }

            if (status == kLiteRtStatusOk) {
                val rankedType = alloc<LiteRtRankedTensorType>()
                status = LiteRtGetRankedTensorType(
                    tensor = tensorRef.value!!,
                    ranked_tensor_type = rankedType.ptr
                )
                if (status == kLiteRtStatusOk) {
                    return rankedType.toPlatform()
                }
            }

            throw Exception(message = "${if (isInput) "Input" else "Output"} tensor $name not found in signature $signatureIndex.")
        }
    }

    private fun LiteRtRankedTensorType.toPlatform(): LiteRTTensorType {
        val platformType = LiteRTElementType.fromNative(this.element_type.toInt())
        return LiteRTTensorType(
            elementType = platformType,
            layout = this.layout.toPlatform()
        )
    }

    private fun getSignatureTensorName(
        signature: LiteRtSignature,
        index: Int,
        isInput: Boolean
    ): String? = memScoped {
        val nameRef = alloc<CPointerVar<ByteVar>>()
        val status = if (isInput) {
            LiteRtGetSignatureInputName(signature, index.toULong(), nameRef.ptr)
        } else {
            LiteRtGetSignatureOutputName(signature, index.toULong(), nameRef.ptr)
        }
        check(status == kLiteRtStatusOk) { "Failed to get tensor name at index $index: $status" }
        nameRef.value?.toKString()
    }

    private fun getTensorName(tensor: LiteRtTensor): String? = memScoped {
        val nameRef = alloc<CPointerVar<ByteVar>>()
        val status = LiteRtGetTensorName(tensor, nameRef.ptr)
        check(status == kLiteRtStatusOk) { "Failed to get tensor name: $status" }
        nameRef.value?.toKString()
    }

    private fun LiteRtLayout.toPlatform(): LiteRTLayout {
        val rank = LiteRtGetLayoutRank(layout = this.ptr)
        val hasStrides = LiteRtGetLayoutHasStrides(layout = this.ptr)

        val dims = mutableListOf<Int>()
        for (i in 0 until rank) {
            dims.add(this.dimensions[i])
        }

        val strides = if (hasStrides) {
            val list = mutableListOf<Int>()
            for (i in 0 until rank) {
                list.add(element = this.strides[i].toInt())
            }
            list
        } else {
            LiteRTLayout.calculateDefaultStrides(dimensions = dims)
        }

        return LiteRTLayout(dimensions = dims, strides = strides)
    }

    private fun getBuffers(signatureIndex: Int, isInput: Boolean): List<TFBuffer> {
        return memScoped {
            val signatureRef = alloc<LiteRtSignatureVar>()
            var status = LiteRtGetModelSignature(
                model = model,
                signature_index = signatureIndex.toULong(),
                signature = signatureRef.ptr
            )
            check(value = status == kLiteRtStatusOk) {
                "Failed to get signature: $status"
            }

            val signature = signatureRef.value!!
            val numTensorsRef = alloc<LiteRtParamIndexVar>()
            status = if (isInput) {
                LiteRtGetNumSignatureInputs(signature = signature, num_inputs = numTensorsRef.ptr)
            } else {
                LiteRtGetNumSignatureOutputs(signature = signature, num_outputs = numTensorsRef.ptr)
            }
            check(value = status == kLiteRtStatusOk) {
                "Failed to get num tensors: $status"
            }

            val numTensors = numTensorsRef.value.toInt()
            val buffers = mutableListOf<TFBuffer>()
            for (i in 0 until numTensors) {
                val bufferRequirementsRef = alloc<LiteRtTensorBufferRequirementsVar>()
                status = if (isInput) {
                    LiteRtGetCompiledModelInputBufferRequirements(
                        compiled_model = compiledModel,
                        signature_index = signatureIndex.toULong(),
                        input_index = i.toULong(),
                        buffer_requirements = bufferRequirementsRef.ptr
                    )
                } else {
                    LiteRtGetCompiledModelOutputBufferRequirements(
                        compiled_model = compiledModel,
                        signature_index = signatureIndex.toULong(),
                        output_index = i.toULong(),
                        buffer_requirements = bufferRequirementsRef.ptr
                    )
                }
                check(value = status == kLiteRtStatusOk) {
                    "Failed to get buffer requirements: $status"
                }

                val tensorRef = alloc<LiteRtTensorVar>()
                status = if (isInput) {
                    LiteRtGetSignatureInputTensorByIndex(
                        signature = signature,
                        input_idx = i.toULong(),
                        tensor = tensorRef.ptr
                    )
                } else {
                    LiteRtGetSignatureOutputTensorByIndex(
                        signature = signature,
                        output_idx = i.toULong(),
                        tensor = tensorRef.ptr
                    )
                }
                check(value = status == kLiteRtStatusOk) {
                    "Failed to get tensor: $status"
                }

                val rankedType = alloc<LiteRtRankedTensorType>()
                status = LiteRtGetRankedTensorType(
                    tensor = tensorRef.value!!,
                    ranked_tensor_type = rankedType.ptr
                )
                check(value = status == kLiteRtStatusOk) {
                    "Failed to get ranked tensor type: $status"
                }

                val bufferRef = alloc<LiteRtTensorBufferVar>()
                status = LiteRtCreateManagedTensorBufferFromRequirements(
                    env = environment,
                    tensor_type = rankedType.ptr,
                    requirements = bufferRequirementsRef.value!!,
                    buffer = bufferRef.ptr
                )
                check(value = status == kLiteRtStatusOk) {
                    "Failed to create managed tensor buffer: $status"
                }

                buffers.add(element = NativeTFBuffer(buffer = bufferRef.value!!))
            }
            buffers
        }
    }

    companion object {
        fun create(filePath: String, accelerator: LiteRTAccelerator): NativeLiteRtCompiledModel {
            return memScoped {
                val envRef = alloc<LiteRtEnvironmentVar>()
                val statusEnv = LiteRtCreateEnvironment(
                    num_options = 0,
                    options = null,
                    environment = envRef.ptr
                )
                check(value = statusEnv == kLiteRtStatusOk) {
                    "Failed to create environment: $statusEnv"
                }

                val env = envRef.value!!
                val modelRef = alloc<LiteRtModelVar>()
                val statusModel = LiteRtCreateModelFromFile(
                    environment = env,
                    filename = filePath,
                    model = modelRef.ptr
                )
                check(value = statusModel == kLiteRtStatusOk) {
                    "Failed to create model: $statusModel"
                }

                val model = modelRef.value!!
                val optionsRef = alloc<LiteRtOptionsVar>()
                val statusOpt = LiteRtCreateOptions(options = optionsRef.ptr)
                check(value = statusOpt == kLiteRtStatusOk) {
                    "Failed to create options: $statusOpt"
                }

                val options = optionsRef.value!!
                val hwAcc = accelerator.toNative()
                val statusSetOptions = LiteRtSetOptionsHardwareAccelerators(
                    options = options,
                    hardware_accelerators = hwAcc
                )
                check(value = statusSetOptions == kLiteRtStatusOk) {
                    "Failed to set hardware accelerators: $statusSetOptions"
                }

                val compiledModelRef = alloc<LiteRtCompiledModelVar>()
                val statusCompiled = LiteRtCreateCompiledModel(
                    environment = env,
                    model = model,
                    compilation_options = options,
                    compiled_model = compiledModelRef.ptr
                )
                check(value = statusCompiled == kLiteRtStatusOk) {
                    "Failed to create compiled model: $statusCompiled"
                }

                LiteRtDestroyOptions(options = options)

                NativeLiteRtCompiledModel(
                    compiledModel = compiledModelRef.value!!,
                    environment = env,
                    model = model
                )
            }
        }
    }
}
