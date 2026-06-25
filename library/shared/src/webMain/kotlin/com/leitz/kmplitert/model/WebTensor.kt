package com.leitz.kmplitert.model

import org.khronos.webgl.ArrayBufferView

interface WebTensor {
    val arrayBufferView: ArrayBufferView
}

expect fun getWebTensor(arrayBufferView: ArrayBufferView): WebTensor