@file:JsModule("@litertjs/core")
@file:Suppress("FunctionName")
@file:OptIn(ExperimentalWasmJsInterop::class)

package com.leitz.kmplitert.model

import kotlin.js.Promise

external interface CompiledModel: JsAny {
    fun getInputDetails(): JsArray<TensorDetails>
    fun getOutputDetails(): JsArray<TensorDetails>
    fun run(tensor: JsArray<Tensor>): Promise<JsArray<Tensor>>
    fun delete()
}