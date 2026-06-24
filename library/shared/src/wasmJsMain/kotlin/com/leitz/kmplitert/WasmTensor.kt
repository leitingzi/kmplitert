@file:JsModule("@litertjs/core")
@file:OptIn(ExperimentalWasmJsInterop::class)

package com.leitz.kmplitert

import org.khronos.webgl.ArrayBufferView

external class Tensor <T: ArrayBufferView> (val data: T, val shape: JsArray<JsNumber>)

