@file:OptIn(ExperimentalWasmJsInterop::class)

package com.leitz.kmplitert

import com.leitz.kmplitert.model.CompiledModel
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.Promise
import kotlin.js.js

expect fun loadLiteRt(path: String): Promise<JsAny>

expect fun loadAndCompile(model: String, accelerator: JsAny): Promise<CompiledModel>

fun createAccelerator(name: String): JsAny =
    js("({ accelerator: name })")