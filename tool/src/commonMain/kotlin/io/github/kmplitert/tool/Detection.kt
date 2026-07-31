package io.github.kmplitert.tool

/**
 * Represents a single detection result.
 *
 * @property boundingBox The bounding box of the detected object.
 * @property categories A list of possible categories for the detected object, sorted by score.
 */
data class Detection(
    val boundingBox: BoundingBox,
    val categories: List<Category>
)
