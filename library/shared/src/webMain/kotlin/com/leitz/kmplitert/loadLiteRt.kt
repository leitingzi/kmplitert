package com.leitz.kmplitert

import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.Promise

@OptIn(ExperimentalWasmJsInterop::class)
internal expect fun loadLiteRt(path: String): Promise<JsAny>