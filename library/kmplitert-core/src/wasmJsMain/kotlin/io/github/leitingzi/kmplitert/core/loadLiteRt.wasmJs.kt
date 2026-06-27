@file:JsModule("@litertjs/core")
@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.leitingzi.kmplitert.core

import io.github.leitingzi.kmplitert.core.model.CompiledModel
import kotlin.js.Promise


external fun loadLiteRt(path: String): Promise<JsAny>

external fun loadAndCompile(model: String, compileOptions: JsAny): Promise<CompiledModel>
