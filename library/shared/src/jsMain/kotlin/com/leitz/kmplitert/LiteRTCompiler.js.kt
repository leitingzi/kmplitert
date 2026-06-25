@file:OptIn(ExperimentalWasmJsInterop::class)

package com.leitz.kmplitert

import com.leitz.kmplitert.model.Accelerator
import com.leitz.kmplitert.model.CompiledModel
import kotlinx.coroutines.await

actual class LiteRTCompiler {
    private lateinit var compiledModel: CompiledModel

    actual suspend fun init(filePath: String) {
        LiteRtInit.awaitInit()

        val load = loadAndCompile(
            model = filePath,
            compileOptions = Accelerator.create(Accelerator.CPU)
        )

        compiledModel = load.await()

        println("LiteRTCompiler $compiledModel")
    }

    actual suspend fun getInputBuffers(): List<TFBuffer> {
        val inputs = compiledModel.getInputDetails()
        val list = mutableListOf<TFBuffer>()
        for (i in 0 until inputs.length) {
            val details = inputs[i]
            list.add(JsTFBuffer(details.shape))
        }
        println("getInputBuffers ${list.size}")
        return list
    }

    actual suspend fun getOutputBuffers(): List<TFBuffer> {
        val outputs = compiledModel.getOutputDetails()
        val list = mutableListOf<TFBuffer>()
        for (i in 0 until outputs.length) {
            val details = outputs[i]
            list.add(JsTFBuffer(details.shape))
        }
        println("getOutputBuffers ${list.size}")
        return list
    }

    actual suspend fun run(inputs: List<TFBuffer>, outputs: List<TFBuffer>) {
        val inputs = inputs.map {
            (it as JsTFBuffer).tensor
        }.toJsArray()

        val promise = compiledModel.run(inputs)
        val modelOutputs = promise.await()

        modelOutputs.forEach { tensor ->
            outputs.forEach { buffer ->
                (buffer as JsTFBuffer).tensor = tensor
            }
        }
    }

    actual suspend fun close() {
        compiledModel.delete()
    }
}

