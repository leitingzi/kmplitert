@file:JsModule("@litertjs/core")
@file:JsNonModule
@file:OptIn(ExperimentalWasmJsInterop::class)
package com.leitz.kmplitert.model

import org.khronos.webgl.ArrayBufferView
import org.khronos.webgl.Int32Array

external class Tensor {
    constructor(data: ArrayBufferView, shape: Int32Array?)
    fun toTypedArray(): ArrayBufferView
    fun delete()
}
