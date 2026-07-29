package io.github.kmplitert.tool.gesture

/**
 * Represents a recognized hand gesture.
 *
 * @param label The name of the gesture (e.g., "Pinch", "Open Hand").
 * @param score The confidence score of the gesture.
 * @param index The index of the gesture in the model's output labels.
 */
data class Gesture(
    val label: String,
    val score: Float,
    val index: Int
)
