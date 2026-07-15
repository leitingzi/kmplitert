package io.github.leitingzi.kmplitert.core

class ModelTestConfig(
    val name: String,
    val modelBytes: ByteArray,
    val inputs: List<TensorExpectation>,
    val outputs: List<TensorExpectation>,
    val accelerators: List<LiteRTAccelerator> = listOf(LiteRTAccelerator.CPU)
)

class TensorExpectation(
    val name: String,
    val elementType: LiteRTElementType,
    val dimensions: List<Int>? = null,
    val testData: Any? = null, // e.g. FloatArray for input
    val expectedValue: Any? = null, // e.g. FloatArray for output
    val tolerance: Float = 0.001f
)
