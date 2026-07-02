@file:JsModule("@litertjs/core")
@file:JsNonModule
@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.leitingzi.kmplitert.core.model

import org.khronos.webgl.ArrayBufferView
import org.khronos.webgl.Int32Array
import kotlin.js.Promise

external class Tensor {
    constructor(data: ArrayBufferView, shape: Int32Array?)
    val liteRtTensorBuffer: LiteRtTensorBuffer
    fun data(): Promise<ArrayBufferView>
    fun delete()
}


