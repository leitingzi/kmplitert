@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.leitingzi.kmplitert.core

import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.js

expect fun <T> arrayToString(array: T): String

internal fun jsTypeOf(o: JsAny): String = js("typeof o")

internal fun getUint8ArrayElement(array: JsAny, index: Int): Int = js("array[index]")
