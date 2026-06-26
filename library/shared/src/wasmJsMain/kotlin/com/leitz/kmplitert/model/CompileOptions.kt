@file:JsModule("@litertjs/core")
@file:OptIn(ExperimentalWasmJsInterop::class)

package com.leitz.kmplitert.model

external interface CompileOptions: JsAny {
    val environment: Environment?
    val cpuOptions: CpuOptions?
    val gpuOptions: LiteRtGpuOptions?
    val webNNOptions: LiteRtWebNNOptions?
}