package com.leitz.kmplitert.model

import org.khronos.webgl.ArrayBufferView

actual fun getWebTensor(arrayBufferView: ArrayBufferView): WebTensor {
    return object : WebTensor {
        private val tensor = Tensor(arrayBufferView)
        override val arrayBufferView: ArrayBufferView
            get() = tensor.data

    }
}