@file:JsModule("@litertjs/core")
@file:JsNonModule

package com.leitz.kmplitert

import org.khronos.webgl.ArrayBufferView

external class Tensor <T: ArrayBufferView> (val data: T, val shape: Array<Int>)

