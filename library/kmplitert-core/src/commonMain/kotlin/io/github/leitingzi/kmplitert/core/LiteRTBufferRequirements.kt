package io.github.leitingzi.kmplitert.core

data class LiteRTBufferRequirements(
    val supportedTypes: List<LiteRTTensorBufferType>,
    val bufferSize: Int,
    val strides: List<Int>
)
