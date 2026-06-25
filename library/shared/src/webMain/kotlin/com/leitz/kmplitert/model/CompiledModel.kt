@file:OptIn(ExperimentalWasmJsInterop::class)

package com.leitz.kmplitert.model

import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.JsArray

external class CompiledModel: JsAny {
    fun getInputDetails(): JsArray<TensorDetails>
    fun getOutputDetails(): JsArray<TensorDetails>
    fun delete()
}
