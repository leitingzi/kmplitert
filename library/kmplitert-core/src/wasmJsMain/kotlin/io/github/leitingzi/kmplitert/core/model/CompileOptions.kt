@file:JsModule("@litertjs/core")
@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.leitingzi.kmplitert.core.model

external interface CompileOptions: JsAny {
    val environment: Environment?
    val cpuOptions: CpuOptions?
    val gpuOptions: LiteRtGpuOptions?
    val webNNOptions: LiteRtWebNNOptions?
}