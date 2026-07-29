package io.github.kmplitert.tool.pose

/**
 * Represents a single pose landmark (joint).
 *
 * @param x The normalized x-coordinate (0.0 to 1.0).
 * @param y The normalized y-coordinate (0.0 to 1.0).
 * @param z The normalized z-coordinate, representing depth.
 * @param score The confidence score of the landmark.
 */
data class PoseLandmark(
    val x: Float,
    val y: Float,
    val z: Float,
    val score: Float
)
