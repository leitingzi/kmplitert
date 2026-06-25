@file:JsModule("@litertjs/core")
@file:JsNonModule

package com.leitz.kmplitert

import org.khronos.webgl.ArrayBufferView

import kotlin.js.JsAny

external class Tensor <T: ArrayBufferView> (val data: T, val shape: Array<Int>): JsAny
