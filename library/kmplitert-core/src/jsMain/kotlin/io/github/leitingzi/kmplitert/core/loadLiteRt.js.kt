@file:JsModule("@litertjs/core")
@file:JsNonModule
@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.leitingzi.kmplitert.core

import com.leitz.kmplitert.model.CompiledModel
import kotlin.js.Promise

external fun loadLiteRt(path: String): Promise<JsAny>

external fun loadAndCompile(model: String, compileOptions: JsAny): Promise<CompiledModel>
