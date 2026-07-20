package io.github.leitingzi.kmplitert.tool.text

/**
 * Represents an answer returned by the Question Answering API.
 *
 * @property text The answer text.
 * @property score The confidence score of this answer.
 */
data class QaAnswer(
    val text: String,
    val score: Float
)
