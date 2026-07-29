package io.github.kmplitert.tool.face

/**
 * Represents a recognized face expression or emotion.
 *
 * @param label The name of the expression (e.g., "Smile", "Sad").
 * @param score The confidence score of the expression.
 */
data class FaceExpression(
    val label: String,
    val score: Float
)
