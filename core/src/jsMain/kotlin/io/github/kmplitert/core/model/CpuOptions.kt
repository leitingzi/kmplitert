@file:JsModule("@litertjs/core")
@file:JsNonModule
@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.kmplitert.core.model

external interface CpuOptions: JsAny {
    val numThreads: Int?
}




