package io.github.kmplitert.tool.expand

import kotlin.math.sqrt

/**
 * Calculates the mean value of the [FloatArray].
 *
 * Returns 0f if the array is empty.
 */
fun FloatArray.mean(): Float = if (isEmpty()) 0f else sum() / size

/**
 * Calculates the standard deviation of the [FloatArray].
 */
fun FloatArray.std(): Float {
    if (size <= 1) return 0f
    val m = mean()
    val variance = fold(0f) { acc, value -> acc + (value - m) * (value - m) } / size
    return sqrt(variance)
}

/**
 * Calculates the Cosine Similarity between two [FloatArray] vectors.
 *
 * @throws IllegalArgumentException if the sizes do not match.
 */
fun FloatArray.cosineSimilarity(other: FloatArray): Float {
    require(size == other.size) { "Size mismatch: $size vs ${other.size}" }
    var dotProduct = 0f
    var normA = 0f
    var normB = 0f
    for (i in indices) {
        dotProduct += this[i] * other[i]
        normA += this[i] * this[i]
        normB += other[i] * other[i]
    }
    val denominator = sqrt(normA) * sqrt(normB)
    return if (denominator > 0) dotProduct / denominator else 0f
}

/**
 * Calculates the Euclidean Distance between two [FloatArray] vectors.
 *
 * @throws IllegalArgumentException if the sizes do not match.
 */
fun FloatArray.euclideanDistance(other: FloatArray): Float {
    require(size == other.size) { "Size mismatch: $size vs ${other.size}" }
    var sum = 0f
    for (i in indices) {
        val diff = this[i] - other[i]
        sum += diff * diff
    }
    return sqrt(sum)
}
