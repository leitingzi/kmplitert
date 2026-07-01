@file:JsModule("@litertjs/core")
@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.leitingzi.model

external interface LiteRtCompileOptions: JsAny {
    val accelerator: String? // 'wasm'|'webgpu'|'webnn'
    val gpuOptions: LiteRtGpuOptions?
    val webNNOptions: LiteRtWebNNOptions?
}