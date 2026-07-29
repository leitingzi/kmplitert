package io.github.kmplitert.tool.face

/**
 * Configuration options for [FaceAnalyzer].
 *
 * @param scoreThreshold The minimum score for expressions/landmarks to be considered.
 * @param expressionLabels Optional labels for expression classification output indices.
 * @param mean The mean value for input normalization.
 * @param std The standard deviation for input normalization.
 */
data class FaceAnalyzerOptions(
    val scoreThreshold: Float = 0.5f,
    val expressionLabels: List<String>? = null,
    val mean: Float = 0f,
    val std: Float = 255f
)
