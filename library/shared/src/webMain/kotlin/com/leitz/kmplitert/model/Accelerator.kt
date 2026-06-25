package com.leitz.kmplitert.model

import com.leitz.kmplitert.createAccelerator
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny

enum class Accelerator(val value: String) {
    CPU("wasm"),
    GPU("webgpu"),
    NPU("webnn");

    companion object {
        @OptIn(ExperimentalWasmJsInterop::class)
        fun create(accelerator: Accelerator): JsAny {
            return createAccelerator(accelerator.value)
        }
    }
}