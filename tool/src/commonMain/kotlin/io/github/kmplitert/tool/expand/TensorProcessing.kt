package io.github.kmplitert.tool.expand

import kotlin.math.roundToInt

/* ---------- Data Type Conversion ---------- */

/**
 * Converts a [ByteArray] to a [FloatArray].
 */
fun ByteArray.toFloatArray(): FloatArray = FloatArray(size) { this[it].toFloat() }

/**
 * Converts a [FloatArray] to a [ByteArray] by rounding each element to the nearest [Byte].
 */
fun FloatArray.toByteArray(): ByteArray = ByteArray(size) { this[it].roundToInt().toByte() }

/**
 * Converts an [IntArray] to a [FloatArray].
 */
fun IntArray.toFloatArray(): FloatArray = FloatArray(size) { this[it].toFloat() }

/**
 * Converts a [FloatArray] to an [IntArray] by rounding each element to the nearest [Int].
 */
fun FloatArray.toIntArray(): IntArray = IntArray(size) { this[it].roundToInt() }

/**
 * Converts a [BooleanArray] to a [ByteArray] (1 for true, 0 for false).
 */
fun BooleanArray.toByteArray(): ByteArray = ByteArray(size) { if (this[it]) 1 else 0 }

/**
 * Converts a [ByteArray] to a [BooleanArray] (true if non-zero).
 */
fun ByteArray.toBooleanArray(): BooleanArray = BooleanArray(size) { this[it].toInt() != 0 }

/* ---------- Tensor Normalization & Quantization ---------- */

/**
 * Normalizes the [FloatArray] using the given [mean] and [std].
 *
 * Formula: `(value - mean) / std`
 *
 * @throws IllegalArgumentException if [std] is zero.
 */
fun FloatArray.normalize(mean: Float, std: Float): FloatArray {
    require(std != 0f) {
        "std must not be zero."
    }
    return FloatArray(size) { (this[it] - mean) / std }
}

/**
 * Clamps the values in the [FloatArray] within the range `[min, max]`.
 */
fun FloatArray.clamp(min: Float, max: Float): FloatArray {
    require(min <= max) {
        "min must be <= max."
    }
    return FloatArray(size) {
        when {
            this[it] < min -> min
            this[it] > max -> max
            else -> this[it]
        }
    }
}

/**
 * Quantizes the [FloatArray] into a [ByteArray] using the given [scale] and [zeroPoint].
 *
 * Formula: `round(value / scale + zeroPoint)` coerced to `[-128, 127]`.
 */
fun FloatArray.quantize(scale: Float, zeroPoint: Int): ByteArray {
    require(scale > 0f) {
        "scale must be > 0."
    }
    return ByteArray(size) {
        val q = (this[it] / scale + zeroPoint).roundToInt().coerceIn(-128, 127)
        q.toByte()
    }
}

/**
 * Dequantizes the [ByteArray] into a [FloatArray] using the given [scale] and [zeroPoint].
 *
 * Formula: `(value - zeroPoint) * scale`
 */
fun ByteArray.dequantize(scale: Float, zeroPoint: Int): FloatArray {
    require(scale > 0f) {
        "scale must be > 0."
    }
    return FloatArray(size) {
        (this[it].toInt() - zeroPoint) * scale
    }
}

/* ---------- Shape & Padding ---------- */

/**
 * Adds padding to the [FloatArray] before and after its current content.
 *
 * @param before Number of padding elements to add before.
 * @param after Number of padding elements to add after.
 * @param value The value to use for padding (defaults to 0f).
 * @return A new [FloatArray] with padding.
 */
fun FloatArray.pad(before: Int, after: Int, value: Float = 0f): FloatArray {
    val result = FloatArray(size + before + after) { value }
    this.copyInto(result, before)
    return result
}

/**
 * Simulates a reshape operation on the [FloatArray].
 *
 * Since Kotlin/KMP arrays are flat, this primarily validates if the current [size]
 * matches the [expectedSize] calculated from [dims].
 *
 * @throws IllegalArgumentException if the size does not match.
 */
fun FloatArray.reshape(vararg dims: Int): FloatArray {
    val expectedSize = dims.fold(1) {
        acc, d -> acc * d
    }

    require(size == expectedSize) {
        "Size mismatch: current $size, expected $expectedSize"
    }
    return this
}
