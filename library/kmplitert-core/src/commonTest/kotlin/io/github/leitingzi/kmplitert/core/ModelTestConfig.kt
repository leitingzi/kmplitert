package io.github.leitingzi.kmplitert.core

class ModelTestConfig(
    val name: String,
    val modelBytes: ByteArray,
    val inputs: List<TensorExpectation>,
    val outputs: List<TensorExpectation>,
    val accelerators: List<LiteRTAccelerator> = listOf(LiteRTAccelerator.CPU)
)
