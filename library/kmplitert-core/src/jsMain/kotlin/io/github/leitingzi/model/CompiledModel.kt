@file:JsModule("@litertjs/core")
@file:JsNonModule
@file:Suppress("FunctionName")
@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.leitingzi.model

import kotlin.js.Promise

external interface CompiledModel : JsAny {
    fun getInputDetails(): JsArray<TensorDetails>
    fun getOutputDetails(): JsArray<TensorDetails>
    fun run(tensor: JsArray<Tensor>): Promise<JsArray<Tensor>>
    fun delete()
}