package io.github.kmplitert.tool.face

/**
 * Represents the 3D orientation of a face.
 *
 * @param pitch The pitch angle in degrees (up/down rotation).
 * @param yaw The yaw angle in degrees (left/right rotation).
 * @param roll The roll angle in degrees (tilt rotation).
 */
data class FaceOrientation(
    val pitch: Float,
    val yaw: Float,
    val roll: Float
)
