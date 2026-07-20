package io.github.leitingzi.kmplitert.tool.detection

/**
 * Represents a bounding box in 2D space.
 *
 * @property left The leftmost coordinate.
 * @property top The topmost coordinate.
 * @property right The rightmost coordinate.
 * @property bottom The bottommost coordinate.
 */
data class BoundingBox(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    /** The width of the bounding box. */
    val width: Float get() = right - left

    /** The height of the bounding box. */
    val height: Float get() = bottom - top

    /** The area of the bounding box. */
    val area: Float get() = width * height
}
