@file:JsModule("@litertjs/core")
@file:JsNonModule
@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.leitingzi.model

external interface LiteRtWebNNOptions: JsAny {
    val devicePreference: String? // 'cpu'|'gpu'|'npu'
    val powerPreference: String? // 'default'|'high-performance'|'low-power'
    val precision: String? // 'fp32'|'fp16'
}