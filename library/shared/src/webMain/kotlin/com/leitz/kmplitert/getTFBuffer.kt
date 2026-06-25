package com.leitz.kmplitert

import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny

@OptIn(ExperimentalWasmJsInterop::class)
interface WebTFBuffer: TFBuffer {
    var tensor: JsAny?
}

expect fun getBuffer(): WebTFBuffer
