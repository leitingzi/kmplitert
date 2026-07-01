@file:JsModule("@litertjs/core")
@file:JsNonModule
@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.leitingzi.model

external fun isWebGPUSupported(): Boolean

external interface LiteRtGpuOptions: JsAny {
    val precision: String? // 'fp16'|'fp32'
}