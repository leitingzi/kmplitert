package io.github.kmplitert.core

class TensorExpectation(
    val name: String,
    val elementType: LiteRTElementType,
    val dimensions: List<Int>? = null,
    val testData: Any? = null,
    val expectedValue: Any? = null,
    val tolerance: Float = 0.001f
)
