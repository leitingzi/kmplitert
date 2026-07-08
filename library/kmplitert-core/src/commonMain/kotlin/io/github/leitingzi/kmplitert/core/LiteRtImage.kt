@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.leitingzi.kmplitert.core

/**
 * Represents an image that can be preprocessed before being fed into a LiteRT model.
 *
 * This class provides common image operations such as resizing and converting
 * pixel data into a normalized [FloatArray].
 *
 * Platform-specific implementations are provided for each supported target.
 */
expect class LiteRtImage {

    /**
     * Returns a resized copy of this image.
     *
     * The original image is not modified.
     *
     * @param width The target image width in pixels.
     * @param height The target image height in pixels.
     * @return A new [LiteRtImage] with the specified dimensions.
     */
    fun resize(width: Int, height: Int): LiteRtImage

    /**
     * Converts the image into a normalized float array.
     *
     * Each pixel channel is converted using the following formula:
     *
     * `normalized = (value - mean) / std`
     *
     * where `value` is the original channel value in the range `[0, 255]`.
     *
     * The output array is arranged in platform-defined channel order
     * (typically RGB) and is suitable for use as model input.
     *
     * @param mean The value subtracted from each channel. Defaults to `0f`.
     * @param std The value used to divide each channel after subtraction.
     * Defaults to `1f`.
     * @return A normalized float array containing the image pixel data.
     */
    fun toFloatArray(mean: Float = 0f, std: Float = 1f): FloatArray

    /**
     * Converts the image into a signed 8-bit integer array (ByteArray).
     *
     * Each pixel channel (0-255) is cast directly to a signed [Byte].
     * This is commonly used for quantized models.
     *
     * @return A byte array containing the image pixel data in RGB order.
     */
    fun toInt8Array(): ByteArray

    /**
     * Converts the image into an integer array.
     *
     * Each pixel channel (0-255) is stored as an [Int].
     *
     * @return An integer array containing the image pixel data in RGB order.
     */
    fun toIntArray(): IntArray

    /**
     * Converts the image into a boolean array.
     *
     * Each pixel channel is converted to boolean: `true` if > 127, `false` otherwise.
     *
     * @return A boolean array containing the image pixel data in RGB order.
     */
    fun toBooleanArray(): BooleanArray

    /**
     * Converts the image into a long array.
     *
     * Each pixel channel (0-255) is stored as a [Long].
     *
     * @return A long array containing the image pixel data in RGB order.
     */
    fun toLongArray(): LongArray

    companion object {

        /**
         * Decodes an image from its encoded byte representation.
         *
         * Supported image formats depend on the underlying platform
         * implementation (for example PNG or JPEG).
         *
         * @param bytes The encoded image bytes.
         * @return A decoded [LiteRtImage].
         * @throws IllegalArgumentException If the image cannot be decoded.
         */
        fun fromBytes(bytes: ByteArray): LiteRtImage

        /**
         * Creates a [LiteRtImage] from raw RGB pixel data.
         *
         * @param data The raw RGB pixel data (3 bytes per pixel).
         * @param width The image width.
         * @param height The image height.
         * @return A [LiteRtImage].
         */
        fun fromRawRgb(data: ByteArray, width: Int, height: Int): LiteRtImage
    }
}


