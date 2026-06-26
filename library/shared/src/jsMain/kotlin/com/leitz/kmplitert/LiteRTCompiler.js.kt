@file:OptIn(ExperimentalWasmJsInterop::class)
@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.leitz.kmplitert

import com.leitz.kmplitert.model.*
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
        val load = loadAndCompile(model = filePath, compileOptions = compileOptions)
        compiledModel = load.await()
    }

    actual suspend fun getInputBuffers(): List<TFBuffer> {
        val inputs = compiledModel.getInputDetails()
        val list = mutableListOf<TFBuffer>()
        for (i in 0 until inputs.length) {
            val details = inputs[i]
            list.add(JsTFBuffer(details.shape))
        }
        return list
    }

    actual suspend fun getOutputBuffers(): List<TFBuffer> {
        val outputs = compiledModel.getOutputDetails()
        val list = mutableListOf<TFBuffer>()
        for (i in 0 until outputs.length) {
            val details = outputs[i]
            list.add(JsTFBuffer(details.shape))
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

