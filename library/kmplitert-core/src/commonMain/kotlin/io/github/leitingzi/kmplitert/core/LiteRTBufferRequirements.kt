package io.github.leitingzi.kmplitert.core

/**
 * Describes the memory requirements for creating a tensor buffer.
 *
 * This information is provided by the runtime and can be used to allocate
 * a compatible buffer for model inputs or outputs.
 *
 * @property supportedTypes The tensor buffer types supported by the runtime
 * for this tensor. The first supported type is typically the preferred one.
 * @property bufferSize The required buffer size in bytes.
 * @property strides The stride of each dimension in bytes. An empty list
 * indicates that the tensor is tightly packed and uses the default contiguous
 * memory layout.
 */
data class LiteRTBufferRequirements(
    val supportedTypes: List<LiteRTTensorBufferType>,
    val bufferSize: Int,
    val strides: List<Int>
)