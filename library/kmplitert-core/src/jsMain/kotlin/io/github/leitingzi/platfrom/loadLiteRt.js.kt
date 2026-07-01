@file:JsModule("@litertjs/core")
@file:JsNonModule
@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.leitingzi.platfrom

import kotlin.js.Promise

external fun loadLiteRt(path: String): Promise<JsAny>

external fun loadAndCompile(model: String, compileOptions: JsAny): Promise<io.github.leitingzi.model.CompiledModel>
