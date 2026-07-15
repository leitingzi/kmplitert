package io.github.leitingzi.kmplitert.core

class TensorExpectation(
    val name: String,
    val elementType: LiteRTElementType,
    val dimensions: List<Int>? = null,
    val testData: Any? = null, // e.g. FloatArray for input
    val expectedValue: Any? = null, // e.g. FloatArray for output
    val tolerance: Float = 0.001f
)