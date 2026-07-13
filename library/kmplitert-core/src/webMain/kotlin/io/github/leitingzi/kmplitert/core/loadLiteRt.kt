package io.github.leitingzi.kmplitert.core

import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.Promise

@OptIn(ExperimentalWasmJsInterop::class)
expect fun loadLiteRt(path: String): Promise<JsAny>