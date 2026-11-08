package io.github.kmplitert.dataprocess.image

/**
 * Represents flipping operations for image processing.
 */
data class ImageFlip(
    val horizontal: Boolean = false,
    val vertical: Boolean = false
)