@file:JsModule("@litertjs/core")
@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.kmplitert.core.model

import org.khronos.webgl.Int32Array
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny

external interface TensorDetails : JsAny {
    val name: String
    val index: Int
    val dtype: String
    val shape: Int32Array
    val supportedBufferTypes: JsSet<JsNumber>
}

external interface JsSet<T : JsAny> : JsAny {
    fun has(value: T): Boolean
    val size: Int
    fun forEach(callback: (T) -> Unit)
}
