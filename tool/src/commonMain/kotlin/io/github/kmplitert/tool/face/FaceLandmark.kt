package io.github.kmplitert.tool.face

/**
 * Represents a single face landmark point in 3D space.
 *
 * @param x The normalized x-coordinate (0.0 to 1.0).
 * @param y The normalized y-coordinate (0.0 to 1.0).
 * @param z The normalized z-coordinate, representing depth.
 */
data class FaceLandmark(
    val x: Float,
    val y: Float,
    val z: Float
)
