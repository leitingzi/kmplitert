package io.github.kmplitert.tool.text

/**
 * Configuration options for text classification.
 *
 * @property topK The maximum number of top classification results to return. If <= 0, all results are returned.
 * @property scoreThreshold Results with a score below this threshold will be discarded.
 * @property labels An optional list of labels corresponding to the output tensor indices.
 */
data class TextClassifierOptions(
    val topK: Int = -1,
    val scoreThreshold: Float = 0f,
    val labels: List<String>? = null
)
