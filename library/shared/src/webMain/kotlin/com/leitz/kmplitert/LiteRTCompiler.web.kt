@file:OptIn(ExperimentalWasmJsInterop::class)

package com.leitz.kmplitert

import com.leitz.kmplitert.model.Accelerator
import com.leitz.kmplitert.model.CompiledModel
import kotlinx.coroutines.await
import kotlin.js.ExperimentalWasmJsInterop

actual class LiteRTCompiler {
    private var model: CompiledModel? = null

    actual suspend fun init(filePath: String) {
        LiteRtInit.awaitInit()

        model = loadAndCompile(
            model = filePath,
            accelerator = Accelerator.create(Accelerator.CPU)
        ).await()
    }

    actual suspend fun getInputBuffers(): List<TFBuffer> {
        TODO()
    }

    actual suspend fun getOutputBuffers(): List<TFBuffer> {
        TODO()
    }

    actual suspend fun run(inputs: List<TFBuffer>, outputs: List<TFBuffer>) {
        TODO()
    }

    actual suspend fun close() {
        model = null
    }
}