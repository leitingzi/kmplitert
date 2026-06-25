@file:JsModule("@litertjs/core")
@file:JsNonModule
@file:Suppress("FunctionName")
@file:OptIn(ExperimentalWasmJsInterop::class)

package com.leitz.kmplitert.model

import kotlin.js.Promise

external class CompiledModel {
    fun getInputDetails(): JsArray<TensorDetails>
    fun getOutputDetails(): JsArray<TensorDetails>
    fun run(tensor: JsArray<Tensor>): Promise<JsArray<Tensor>>
    fun delete()
}