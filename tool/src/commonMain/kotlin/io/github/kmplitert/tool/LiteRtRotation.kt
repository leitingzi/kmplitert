package io.github.kmplitert.tool

/**
 * Represents a rotation angle for image processing.
 */
enum class LiteRtRotation(val degrees: Int) {
    ROTATION_0(degrees = 0),
    ROTATION_90(degrees = 90),
    ROTATION_180(degrees = 180),
    ROTATION_270(degrees = 270)
}