@file:OptIn(ExperimentalForeignApi::class)

package io.github.leitingzi.model

import io.github.leitingzi.LiteRTAccelerator
import io.github.leitingzi.NativeTFBuffer
import io.github.leitingzi.TFBuffer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.set
import kotlinx.cinterop.value
import litert.LiteRtCompiledModel
import litert.LiteRtCompiledModelVar
import litert.LiteRtCreateCompiledModel
import litert.LiteRtCreateEnvironment
import litert.LiteRtCreateManagedTensorBufferFromRequirements
import litert.LiteRtCreateModelFromFile
import litert.LiteRtCreateOptions
import litert.LiteRtDestroyCompiledModel
import litert.LiteRtDestroyEnvironment
import litert.LiteRtDestroyModel
import litert.LiteRtDestroyOptions
import litert.LiteRtEnvironment
import litert.LiteRtEnvironmentVar
import litert.LiteRtGetCompiledModelInputBufferRequirements
import litert.LiteRtGetCompiledModelOutputBufferRequirements
import litert.LiteRtGetModelSignature
import litert.LiteRtGetNumSignatureInputs
import litert.LiteRtGetNumSignatureOutputs
import litert.LiteRtGetRankedTensorType
import litert.LiteRtGetSignatureInputTensorByIndex
import litert.LiteRtGetSignatureOutputTensorByIndex
import litert.LiteRtModel
import litert.LiteRtModelVar
import litert.LiteRtOptionsVar
import litert.LiteRtParamIndexVar
import litert.LiteRtRankedTensorType
import litert.LiteRtRunCompiledModel
import litert.LiteRtSetOptionsHardwareAccelerators
import litert.LiteRtSignatureVar
import litert.LiteRtTensorBufferRequirementsVar
import litert.LiteRtTensorBufferVar
import litert.LiteRtTensorVar
import litert.kLiteRtStatusOk

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
