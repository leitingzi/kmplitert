@file:OptIn(ExperimentalWasmJsInterop::class)
@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.leitingzi.kmplitert.core

import io.github.leitingzi.kmplitert.core.model.*
import io.github.leitingzi.kmplitert.core.platform.LiteRtInit
import io.github.leitingzi.kmplitert.core.platform.loadAndCompile
import kotlinx.coroutines.await

actual class LiteRTCompiler actual constructor(
    val filePath: String,
    val accelerator: LiteRTAccelerator
) {
    private lateinit var compiledModel: CompiledModel

    actual suspend fun init() {
        LiteRtInit.awaitInit()
//        val compileOptions = getCompileOptions(accelerator = accelerator)
        val compileOptions = getLiteRtCompileOptions(accelerator = accelerator)
        val load = loadAndCompile(
            model = filePath,
            compileOptions = compileOptions
        )
        compiledModel = load.await()
    }

    actual suspend fun getInputTensorType(inputName: String): LiteRTTensorType {
        val details = compiledModel.getInputDetails().toKotlinList().find { it.name == inputName }
            ?: throw IllegalArgumentException("Input tensor $inputName not found")
        return details.toPlatform()
    }

    actual suspend fun getOutputTensorType(outputName: String): LiteRTTensorType {
        val details = compiledModel.getOutputDetails().toKotlinList().find { it.name == outputName }
            ?: throw IllegalArgumentException("Output tensor $outputName not found")
        return details.toPlatform()
    }

    actual suspend fun getInputBufferRequirements(inputName: String): LiteRTBufferRequirements {
        val details = compiledModel.getInputDetails().toKotlinList().find { it.name == inputName }
            ?: throw IllegalArgumentException("Input tensor $inputName not found")
        return details.toRequirements()
    }

    actual suspend fun getOutputBufferRequirements(outputName: String): LiteRTBufferRequirements {
        val details = compiledModel.getOutputDetails().toKotlinList().find { it.name == outputName }
            ?: throw IllegalArgumentException("Output tensor $outputName not found")
        return details.toRequirements()
    }

    private fun TensorDetails.toRequirements(): LiteRTBufferRequirements {
        val supportedTypes = mutableListOf<LiteRTTensorBufferType>()
        supportedBufferTypes.forEach { typeValue ->
            val type = when (typeValue.asDynamic().unsafeCast<Int>()) {
                1 -> LiteRTTensorBufferType.HostMemory
                20 -> LiteRTTensorBufferType.WebGpuBuffer
                21 -> LiteRTTensorBufferType.WebGpuBufferFp16
                26 -> LiteRTTensorBufferType.WebGpuBufferPacked
                else -> LiteRTTensorBufferType.Unknown
            }
            supportedTypes.add(type)
        }

        val dims = mutableListOf<Int>()
        for (i in 0 until shape.length) {
            dims.add(shape.asDynamic()[i].unsafeCast<Int>())
        }

        val elementSize = when (dtype) {
            "float32" -> 4
            "int32" -> 4
            "int64" -> 8
            "bool" -> 1
            "int8" -> 1
            "uint8" -> 1
            else -> 4
        }

        var totalElements = 1
        for (dim in dims) {
            totalElements *= dim
        }

        val bufferSize = totalElements * elementSize

        return LiteRTBufferRequirements(
            supportedTypes = supportedTypes,
            bufferSize = bufferSize,
            strides = LiteRTLayout.calculateDefaultStrides(dims)
        )
    }

    private fun <T : JsAny> JsArray<T>.toKotlinList(): List<T> {
        val list = mutableListOf<T>()
        for (i in 0 until length) {
            list.add(this[i])
        }
        return list
    }

    private fun TensorDetails.toPlatform(): LiteRTTensorType {
        val platformElementType = when (dtype) {
            "float32" -> LiteRTElementType.FLOAT
            "int32" -> LiteRTElementType.INT
            "int64" -> LiteRTElementType.INT64
            "bool" -> LiteRTElementType.BOOLEAN
            "int8" -> LiteRTElementType.INT8
            else -> LiteRTElementType.FLOAT // Default
        }
        val dims = mutableListOf<Int>()
        for (i in 0 until shape.length) {
            dims.add(shape.asDynamic()[i].unsafeCast<Int>())
        }
        return LiteRTTensorType(
            elementType = platformElementType,
            layout = LiteRTLayout(dimensions = dims, strides = LiteRTLayout.calculateDefaultStrides(dims))
        )
    }

    actual suspend fun getInputBuffers(): List<TFBuffer> {
        val inputs = compiledModel.getInputDetails()
        val list = mutableListOf<TFBuffer>()
        for (i in 0 until inputs.length) {
            val details = inputs[i]
            list.add(JsTFBuffer(details.shape, details.dtype))
        }
        return list
    }

    actual suspend fun getOutputBuffers(): List<TFBuffer> {
        val outputs = compiledModel.getOutputDetails()
        val list = mutableListOf<TFBuffer>()
        for (i in 0 until outputs.length) {
            val details = outputs[i]
            list.add(JsTFBuffer(details.shape, details.dtype))
        }
        return list
    }

    actual suspend fun run(inputs: List<TFBuffer>, outputs: List<TFBuffer>) {
        val inputTensors = inputs.map {
            (it as JsTFBuffer).tensor
        }.toJsArray()

        val promise = compiledModel.run(inputTensors)
        val modelOutputs = promise.await()

        for (i in 0 until modelOutputs.length) {
            val outputTensor = modelOutputs[i]
            (outputs[i] as JsTFBuffer).tensor = outputTensor
        }
    }

    actual suspend fun close() {
        compiledModel.delete()
    }

    private fun getCompileOptions(accelerator: LiteRTAccelerator): JsAny {

        val cpuOptions = if (accelerator == LiteRTAccelerator.CPU) {
            createCpuOptions()
        } else {
            null
        }

        val gpuOptions = if (accelerator == LiteRTAccelerator.GPU) {
            if (isWebGPUSupported()) createLiteRtGpuOptions() else null
        } else {
            null
        }

        val webNNOptions = if (accelerator == LiteRTAccelerator.NPU) {
            createLiteRtWebNNOptions()
        } else {
            null
        }

        return createCompileOptions(
            environment = getDefaultEnvironment(),
            cpuOptions = cpuOptions,
            gpuOptions = gpuOptions,
            webNNOptions = webNNOptions,
        )
    }

    private fun getLiteRtCompileOptions(accelerator: LiteRTAccelerator): JsAny {
        return when (accelerator) {
            LiteRTAccelerator.CPU -> {
               createLiteRtCompileOptions(accelerator = "wasm")
            }

            LiteRTAccelerator.GPU -> {
                if (isWebGPUSupported()) {
                    createLiteRtCompileOptions(
                        accelerator = "webgpu",
                        gpuOptions = createLiteRtGpuOptions(),
                        webNNOptions = null
                    )
                } else {
                    createLiteRtCompileOptions(accelerator = "wasm")
                }
            }

            LiteRTAccelerator.NPU -> {
                createLiteRtCompileOptions(
                    accelerator = "webnn",
                    gpuOptions = null,
                    webNNOptions = createLiteRtWebNNOptions()
                )
            }
        }
    }
}



