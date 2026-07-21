package io.github.kmplitert.core

/**
 * Describes the layout of a tensor.
 *
 * @property dimensions Size of each tensor dimension.
 * @property strides Memory stride for each dimension. An empty list indicates
 * the tensor uses the default contiguous layout.
 */
data class LiteRTLayout(val dimensions: List<Int> , val strides: List<Int>) {

    init {
        require(value = strides.isEmpty() || strides.size == dimensions.size) {
            "strides.size must equal dimensions.size or be empty."
        }
    }

    /**
     * Number of tensor dimensions.
     */
    val rank: Int get() = dimensions.size

    /**
     * Whether explicit strides are available.
     */
    val hasStrides: Boolean get() = strides.isNotEmpty()

    companion object {
        /**
         * Calculates default row-major strides for the given dimensions.
         */
        fun calculateDefaultStrides(dimensions: List<Int>): List<Int> {
            if (dimensions.isEmpty()) {
                return emptyList()
            }

            val strides = mutableListOf<Int>()
            var currentStride = 1
            for (i in dimensions.size - 1 downTo 0) {
                strides.add(0, currentStride)
                currentStride *= dimensions[i]
            }
            return strides
        }
    }
}