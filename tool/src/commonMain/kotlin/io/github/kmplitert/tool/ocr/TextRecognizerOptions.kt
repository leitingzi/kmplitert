package io.github.kmplitert.tool.ocr

/**
 * Configuration options for [TextRecognizer].
 *
 * @param minScoreThreshold The minimum score for text detection/recognition to be considered valid.
 * @param mean The mean value for input normalization.
 * @param std The standard deviation for input normalization.
 */
data class TextRecognizerOptions(
    val minScoreThreshold: Float = 0.5f,
    val mean: Float = 127.5f,
    val std: Float = 127.5f
)
