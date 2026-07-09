package io.github.leitingzi.kmplitert.core

/**
 * Describes the type information of a tensor.
 *
 * @property elementType Data type of the tensor elements.
 * @property layout Layout information of the tensor. This is `null` when the layout is unknown or not applicable.
 */
data class LiteRTTensorType(
    val elementType: LiteRTElementType,
    val layout: LiteRTLayout? = null
)