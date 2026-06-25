@file:OptIn(ExperimentalWasmJsInterop::class)

package com.leitz.kmplitert

import com.leitz.kmplitert.model.Accelerator
import com.leitz.kmplitert.model.CompiledModel
import kotlinx.coroutines.await
import org.khronos.webgl.Float32Array

actual class LiteRTCompiler {
    private lateinit var model: CompiledModel

    actual suspend fun init(filePath: String) {
        LiteRtInit.awaitInit()

        model = loadAndCompile(
            model = filePath,
            accelerator = Accelerator.create(Accelerator.CPU)
        ).await()
    }

    actual suspend fun getInputBuffers(): List<TFBuffer> {
        val details = model.getInputDetails()
        val bufferList = mutableListOf<TFBuffer>()

        details.toList().forEach { details ->
            val tensor = Tensor(Float32Array(1), details.shape)
            val buffer = WasmTFBuffer(tensor)
            bufferList.add(buffer)
        }

        println("getInputBuffers: ${bufferList.size}")
        return bufferList
    }

    actual suspend fun getOutputBuffers(): List<TFBuffer> {
        val details = model.getOutputDetails()
        val bufferList = mutableListOf<TFBuffer>()

        details.toList().forEach { details ->
            val tensor = Tensor(Float32Array(1), details.shape)
            val buffer = WasmTFBuffer(tensor)
            bufferList.add(buffer)
        }

        println("getOutputBuffers: ${bufferList.size}")
        return bufferList
    }

    actual suspend fun run(inputs: List<TFBuffer>, outputs: List<TFBuffer>) {
        val inputs = inputs.map {
            (it as WasmTFBuffer).tensor
        }.toJsArray()
        val output = model.run(inputs).await()
        println("output = $output")
    }

    actual suspend fun close() {
        model.delete()
    }
}
