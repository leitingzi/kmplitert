package io.github.kmplitert.tool.classification

/**
 * Represents a single classification category.
 *
 * @property label The label name of this category.
 * @property score The confidence score (probability) for this category, typically in the range [0, 1].
 * @property index The index of this category in the model's output tensor.
 */
data class Category(
    val label: String,
    val score: Float,
    val index: Int
)
