package io.github.kmplitert.tool.gesture

import io.github.kmplitert.core.LiteRTAccelerator

/**
 * Configuration options for [HandGestureRecognizer].
 *
 * @param scoreThreshold The minimum score for a gesture to be considered recognized.
 * @param maxResults The maximum number of gesture results to return.
 * @param labels An optional list of gesture labels mapping to model output indices.
 * @param mean The mean value for input normalization.
 * @param std The standard deviation for input normalization.
 */
data class HandGestureRecognizerOptions(
    val scoreThreshold: Float = 0.5f,
    val maxResults: Int = 5,
    val labels: List<String>? = null,
    val mean: Float = 0f,
    val std: Float = 255f
)
