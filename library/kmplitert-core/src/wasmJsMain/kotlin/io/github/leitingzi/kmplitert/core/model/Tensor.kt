@file:JsModule("@litertjs/core")
@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.leitingzi.kmplitert.core.model

import org.khronos.webgl.ArrayBufferView
import org.khronos.webgl.Int32Array
import kotlin.js.Promise

external class Tensor: JsAny {
    constructor(data: ArrayBufferView, shape: Int32Array?)

    val liteRtTensorBuffer: LiteRtTensorBuffer
    fun data(): Promise<ArrayBufferView>
    fun delete()
}


