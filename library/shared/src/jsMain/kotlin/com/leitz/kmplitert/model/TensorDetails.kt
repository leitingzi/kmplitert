@file:JsModule("@litertjs/core")
@file:JsNonModule
@file:OptIn(ExperimentalWasmJsInterop::class)

package com.leitz.kmplitert.model

import org.khronos.webgl.Int32Array
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny

external interface TensorDetails : JsAny {
    val name: String
    val index: Int
    val dtype: String
    val shape: Int32Array
}