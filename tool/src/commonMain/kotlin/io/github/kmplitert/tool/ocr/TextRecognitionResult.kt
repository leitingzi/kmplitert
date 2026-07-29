package io.github.kmplitert.tool.ocr

import io.github.kmplitert.tool.detection.BoundingBox

/**
 * Represents a single line of recognized text.
 *
 * @param text The recognized text string.
 * @param boundingBox The bounding box containing the line.
 */
data class TextLine(
    val text: String,
    val boundingBox: BoundingBox
)

/**
 * Represents a block (paragraph) of recognized text.
 *
 * @param text The concatenated text of all lines in the block.
 * @param boundingBox The bounding box containing the entire block.
 * @param lines The list of [TextLine]s making up this block.
 */
data class TextBlock(
    val text: String,
    val boundingBox: BoundingBox,
    val lines: List<TextLine>
)

/**
 * The result of a text recognition (OCR) operation.
 *
 * @param text The full recognized text.
 * @param blocks The list of detected [TextBlock]s.
 */
data class TextRecognitionResult(
    val text: String,
    val blocks: List<TextBlock>
)
