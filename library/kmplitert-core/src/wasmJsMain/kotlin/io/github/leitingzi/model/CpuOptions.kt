@file:JsModule("@litertjs/core")
@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.leitingzi.model

external interface CpuOptions: JsAny {
    val numThreads: Int?
}


