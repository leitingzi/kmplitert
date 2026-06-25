@file:JsModule("@litertjs/core")
@file:JsNonModule
@file:OptIn(ExperimentalWasmJsInterop::class)

package com.leitz.kmplitert

import com.leitz.kmplitert.model.CompiledModel
import kotlin.js.Promise

actual external fun loadLiteRt(path: String): Promise<JsAny>

actual external fun loadAndCompile(model: String, accelerator: JsAny): Promise<CompiledModel>
