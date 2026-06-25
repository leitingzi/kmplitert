@file:OptIn(ExperimentalWasmJsInterop::class)

package com.leitz.kmplitert

import com.leitz.kmplitert.model.Accelerator
import com.leitz.kmplitert.model.CompiledModel
import com.leitz.kmplitert.model.getWebTensor
import kotlinx.coroutines.await
import org.khronos.webgl.Float32Array
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.toList

actual class LiteRTCompiler {
    private lateinit var model: CompiledModel

    actual suspend fun init(filePath: String) {
        LiteRtInit.awaitInit()

        model = loadAndCompile(
            model = filePath,
            accelerator = Accelerator.create(Accelerator.CPU)
        ).await()

        println("tensor = ${getWebTensor(Float32Array(1))}")
    }

    actual suspend fun getInputBuffers(): List<TFBuffer> {
        val details = model.getInputDetails()
        val detailsList = details.toList()

        println("Input detailsList: ${detailsList.size}")
        return emptyList()
    }

    actual suspend fun getOutputBuffers(): List<TFBuffer> {
        val details = model.getOutputDetails()
        val detailsList = details.toList()

        println("Output detailsList: ${detailsList.size}")
        return emptyList()
    }

    actual suspend fun run(inputs: List<TFBuffer>, outputs: List<TFBuffer>) {

    }

    actual suspend fun close() {
        model.delete()
    }
}
