@file:OptIn(ExperimentalWasmJsInterop::class)

package com.leitz.kmplitert

import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.js

expect fun <T> arrayToString(array: T): String

fun jsTypeOf(o: JsAny): String = js("typeof o")