package io.github.kmplitert.tool

/**
 * Represents a rotation angle for image processing.
 */
enum class LiteRtRotation(val degrees: Int) {
    ROTATION_0(degrees = 0),
    ROTATION_90(degrees = 90),
    ROTATION_180(degrees = 180),
    ROTATION_270(degrees = 270);

    /**
     * Returns `true` if this rotation swaps the image's width and height.
     *
     * This is `true` for [ROTATION_90] and [ROTATION_270], and `false` otherwise.
     */
    val isLandscapeRotation: Boolean = degrees == 90 || degrees == 270
}