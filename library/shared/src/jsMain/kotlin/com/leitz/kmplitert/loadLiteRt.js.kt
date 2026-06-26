@file:JsModule("@litertjs/core")
@file:JsNonModule
@file:OptIn(ExperimentalWasmJsInterop::class)

package com.leitz.kmplitert

import com.leitz.kmplitert.model.CompileOptions
import com.leitz.kmplitert.model.CompiledModel
import kotlin.js.Promise

external fun loadLiteRt(path: String): Promise<JsAny>

external fun loadAndCompile(model: String, compileOptions: CompileOptions): Promise<CompiledModel>
