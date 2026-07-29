package io.github.kmplitert.tool.segmentation

/**
 * Configuration options for [ImageSegmenter].
 *
 * @param mean The mean value for input normalization.
 * @param std The standard deviation for input normalization.
 */
data class ImageSegmenterOptions(
    val mean: Float = 0f,
    val std: Float = 255f
)
