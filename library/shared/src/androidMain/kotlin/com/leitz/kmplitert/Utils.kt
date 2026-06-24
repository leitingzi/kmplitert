package com.leitz.kmplitert

import com.google.ai.edge.litert.TensorBuffer

fun TFBuffer.toAndroid(): TensorBuffer {
    return (this as AndroidTFBuffer).buffer
}

fun List<TFBuffer>.toAndroid(): List<TensorBuffer> {
    return map { tFBuffer -> tFBuffer.toAndroid() }
}