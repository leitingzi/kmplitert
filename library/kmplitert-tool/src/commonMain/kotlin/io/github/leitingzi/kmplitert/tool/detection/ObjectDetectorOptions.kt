package io.github.leitingzi.kmplitert.tool.detection

/**
 * Options for configuring an [ObjectDetector].
 *
 * @property mean The value subtracted from each channel during normalization. Defaults to 0f.
 * @property std The value used to divide each channel after subtraction during normalization. Defaults to 1f.
 * @property scoreThreshold The minimum score threshold for a result to be included. Defaults to 0f.
 * @property iouThreshold The intersection-over-union threshold for non-maximum suppression. Defaults to 0.5f.
 * @property maxResults The maximum number of top detection results to return. Defaults to -1 (all).
 * @property labels An optional list of label names corresponding to the model's output indices.
 */
data class ObjectDetectorOptions(
    val mean: Float = 0f,
    val std: Float = 1f,
    val scoreThreshold: Float = 0f,
    val iouThreshold: Float = 0.5f,
    val maxResults: Int = -1,
    val labels: List<String>? = null
)
