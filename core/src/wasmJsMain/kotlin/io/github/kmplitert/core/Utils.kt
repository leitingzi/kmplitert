@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.kmplitert.core

internal fun createBigInt64Array(length: Int): JsAny = js("new BigInt64Array(length)")

internal fun setBigInt64Array(array: JsAny, index: Int, value: Long): Unit = js("array[index] = value")

internal fun getBigInt64Array(array: JsAny, index: Int): Long = js("array[index]")

internal fun getBigInt64ArrayLength(array: JsAny): Int = js("array.length")

internal fun getUint8ArrayElement(array: JsAny, index: Int): Int = js("array[index]")