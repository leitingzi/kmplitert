@file:JsModule("@litertjs/core")
@file:JsNonModule
@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.kmplitert.core

import io.github.kmplitert.core.model.CompiledModel
import kotlin.js.Promise

actual external fun loadLiteRt(path: String): Promise<JsAny>

external fun loadAndCompile(model: String, compileOptions: JsAny): Promise<CompiledModel>