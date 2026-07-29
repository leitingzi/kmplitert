package io.github.kmplitert.tool.gesture

/**
 * Represents a single hand landmark point in 3D space.
 *
 * @param x The normalized x-coordinate (0.0 to 1.0).
 * @param y The normalized y-coordinate (0.0 to 1.0).
 * @param z The normalized z-coordinate, representing depth.
 * @param visibility The visibility score of the landmark.
 * @param presence The presence score of the landmark.
 */
data class HandLandmark(
    val x: Float,
    val y: Float,
    val z: Float,
    val visibility: Float = 0f,
    val presence: Float = 0f
)
