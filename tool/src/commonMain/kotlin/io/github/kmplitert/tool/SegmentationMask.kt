package io.github.kmplitert.tool

/**
 * Represents a segmentation mask.
 *
 * @param width Width of the mask.
 * @param height Height of the mask.
 * @param data Flat array of mask values (confidence or category indices).
 */
data class SegmentationMask(
    val width: Int,
    val height: Int,
    val data: FloatArray
) {
    /**
     * Returns the mask value at the specified (x, y) coordinates.
     */
    fun getValue(x: Int, y: Int): Float {
        if (x !in 0..<width || y < 0 || y >= height) return 0f
        return data[y * width + x]
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SegmentationMask) return false
        if (width != other.width) return false
        if (height != other.height) return false
        return data.contentEquals(other.data)
    }

    override fun hashCode(): Int {
        var result = width
        result = 31 * result + height
        result = 31 * result + data.contentHashCode()
        return result
    }
}
