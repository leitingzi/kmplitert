package com.leitz.kmplitert

actual fun getBuffer(): WebTFBuffer {
    return WasmTFBuffer()
}