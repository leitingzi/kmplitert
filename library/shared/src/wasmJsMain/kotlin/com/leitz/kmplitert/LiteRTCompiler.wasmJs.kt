@file:OptIn(ExperimentalWasmJsInterop::class)

package com.leitz.kmplitert

import com.leitz.kmplitert.model.CompiledModel
import com.leitz.kmplitert.model.createCompileOptions
import com.leitz.kmplitert.model.createCpuOptions
import kotlinx.coroutines.await

actual class LiteRTCompiler {
    private lateinit var compiledModel: CompiledModel

    actual suspend fun init(filePath: String) {
        LiteRtInit.awaitInit()
        val load = loadAndCompile(
            model = filePath,
            compileOptions = createCompileOptions(cpuOptions = createCpuOptions(4))
        )
        compiledModel = load.await()
    }

    actual suspend fun getInputBuffers(): List<TFBuffer> {
        val inputs = compiledModel.getInputDetails()
        val list = mutableListOf<TFBuffer>()
        for (i in 0 until inputs.length) {
            val details = inputs[i]
            details?.let {
                list.add(WasmTFBuffer(it.shape))
            }
        }
        return list
    }

    actual suspend fun getOutputBuffers(): List<TFBuffer> {
        val outputs = compiledModel.getOutputDetails()
        val list = mutableListOf<TFBuffer>()
        for (i in 0 until outputs.length) {
            val details = outputs[i]
            details?.let {
                list.add(WasmTFBuffer(it.shape))
            }
        }
        return list
    }

    actual suspend fun run(inputs: List<TFBuffer>, outputs: List<TFBuffer>) {
        val inputTensors = inputs.map {
            (it as WasmTFBuffer).tensor
        }.toJsArray()

        val promise = compiledModel.run(inputTensors)
        val modelOutputs = promise.await()

        for (i in 0 until modelOutputs.length) {
            val outputTensor = modelOutputs[i]
            outputTensor?.let {
                (outputs[i] as WasmTFBuffer).tensor = it
            }
        }
    }

    actual suspend fun close() {
        compiledModel.delete()
    }
}
