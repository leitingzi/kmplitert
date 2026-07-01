@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.leitingzi.kmplitert.core

import kotlin.js.JsAny
import kotlin.js.js

actual fun <T> arrayToString(array: T): String {
    return when (array) {
        is IntArray -> array.contentToString()
        is FloatArray -> array.contentToString()
        is DoubleArray -> array.contentToString()
        is BooleanArray -> array.contentToString()
        is ByteArray -> array.contentToString()
        is ShortArray -> array.contentToString()
        is CharArray -> array.contentToString()
        is LongArray -> array.contentToString()
        else -> ""
    }
}

internal fun createBigInt64Array(length: Int): JsAny = js("new BigInt64Array(length)")

internal fun setBigInt64Array(array: JsAny, index: Int, value: Long): Unit = js("array[index] = value")

internal fun getBigInt64Array(array: JsAny, index: Int): Long = js("array[index]")

internal fun getBigInt64ArrayLength(array: JsAny): Int = js("array.length")
