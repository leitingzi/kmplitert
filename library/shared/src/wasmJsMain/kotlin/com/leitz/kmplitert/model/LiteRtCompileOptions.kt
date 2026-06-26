@file:JsModule("@litertjs/core")
@file:OptIn(ExperimentalWasmJsInterop::class)

package com.leitz.kmplitert.model

external interface LiteRtCompileOptions: JsAny {
    val accelerator: String? // 'wasm'|'webgpu'|'webnn'
    val gpuOptions: LiteRtGpuOptions?
    val webNNOptions: LiteRtWebNNOptions?
}