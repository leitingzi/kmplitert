package io.github.leitingzi.kmplitert.tool.classification

import io.github.leitingzi.kmplitert.core.LiteRTAccelerator

/**
 * Options for configuring an [ImageClassifier].
 *
 * @property accelerator The preferred hardware accelerator. Defaults to CPU.
 * @property mean The value subtracted from each channel during normalization. Defaults to 0f.
 * @property std The value used to divide each channel after subtraction during normalization. Defaults to 1f.
 * @property topK The maximum number of top classification results to return. Defaults to 5.
 * @property scoreThreshold The minimum score threshold for a result to be included. Defaults to 0f.
 * @property labels An optional list of label names corresponding to the model's output indices.
 */
data class ImageClassifierOptions(
    val accelerator: LiteRTAccelerator = LiteRTAccelerator.CPU,
    val mean: Float = 0f,
    val std: Float = 1f,
    val topK: Int = 5,
    val scoreThreshold: Float = 0f,
    val labels: List<String>? = null
)
