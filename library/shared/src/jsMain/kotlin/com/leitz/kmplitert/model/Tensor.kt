@file:JsModule("@litertjs/core")
@file:JsNonModule
@file:OptIn(ExperimentalWasmJsInterop::class)

package com.leitz.kmplitert.model

import org.khronos.webgl.ArrayBufferView

external class Tensor(val data: ArrayBufferView): JsAny