@file:JsModule("@litertjs/core")
@file:OptIn(ExperimentalWasmJsInterop::class)

package com.leitz.kmplitert.model

external interface LiteRtWebNNOptions: JsAny {
    val devicePreference: String? // 'cpu'|'gpu'|'npu'
    val powerPreference: String? // 'default'|'high-performance'|'low-power'
    val precision: String? // 'fp32'|'fp16'
}