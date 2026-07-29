package io.github.kmplitert.tool.segmentation

/**
 * The result of an image segmentation operation.
 *
 * @param mask The generated [SegmentationMask].
 */
data class ImageSegmenterResult(
    val mask: SegmentationMask
)
