@file:JsModule("@litertjs/core")
@file:OptIn(ExperimentalWasmJsInterop::class)

package com.leitz.kmplitert

import org.khronos.webgl.ArrayBufferView

import kotlin.js.JsAny

external class Tensor <T: ArrayBufferView> (val data: T, val shape: JsArray<JsNumber>): JsAny
