package io.github.kmplitert.dataprocess.image

import io.github.kmplitert.core.TFBuffer
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * An image stored as interleaved, row-major grayscale, RGB, or RGBA pixels.
 *
 * [data] and [toInt8Array] return copies of the unsigned 8-bit channel values.
 * The constructor and [fromBytes] decode encoded image bytes; supported formats
 * depend on the platform. Use [fromRawRgb] for unencoded RGB pixels.
 */
class LiteRtImage private constructor(
    private val pixels: ByteArray,
    val width: Int,
    val height: Int,
    val channels: Int,
) {
    private constructor(image: LiteRtImage) : this(image.pixels, image.width, image.height, image.channels)

    constructor(data: ByteArray) : this(decodeImage(data))

    val data: ByteArray get() = pixels.copyOf()

    fun resize(width: Int, height: Int): LiteRtImage {
        val result = ByteArray(pixelCount(width, height, channels))
        for (y in 0 until height) {
            val sy = if (height == 1) 0f else y.toFloat() * (this.height - 1) / (height - 1)
            val y0 = sy.toInt()
            val y1 = (y0 + 1).coerceAtMost(this.height - 1)
            for (x in 0 until width) {
                val sx = if (width == 1) 0f else x.toFloat() * (this.width - 1) / (width - 1)
                val x0 = sx.toInt()
                val x1 = (x0 + 1).coerceAtMost(this.width - 1)
                val dx = sx - x0
                val dy = sy - y0
                for (c in 0 until channels) {
                    fun value(px: Int, py: Int) = pixels[(py * this.width + px) * channels + c].toInt() and 255
                    val top = value(x0, y0) * (1 - dx) + value(x1, y0) * dx
                    val bottom = value(x0, y1) * (1 - dx) + value(x1, y1) * dx
                    result[(y * width + x) * channels + c] = (top * (1 - dy) + bottom * dy).roundToInt().toByte()
                }
            }
        }
        return LiteRtImage(result, width, height, channels)
    }

    fun crop(x: Int, y: Int, width: Int, height: Int): LiteRtImage {
        val size = pixelCount(width, height, channels)
        require(x >= 0 && y >= 0 && width <= this.width && height <= this.height &&
            x <= this.width - width && y <= this.height - height) { "Crop region is outside the image" }
        val result = ByteArray(size)
        for (row in 0 until height) {
            val start = ((y + row) * this.width + x) * channels
            pixels.copyInto(result, row * width * channels, start, start + width * channels)
        }
        return LiteRtImage(result, width, height, channels)
    }

    fun centerCrop(width: Int, height: Int): LiteRtImage {
        require(width <= this.width && height <= this.height) { "Crop region is outside the image" }
        return crop((this.width - width) / 2, (this.height - height) / 2, width, height)
    }

    /** Rotates clockwise, using nearest-neighbor sampling and zero-filled uncovered pixels. */
    fun rotate(degrees: Float): LiteRtImage {
        require(degrees.isFinite()) { "Rotation must be finite" }
        val normalized = ((degrees % 360f) + 360f) % 360f
        val radians = normalized.toDouble() * kotlin.math.PI / 180.0
        val cosine = cos(radians).let { if (abs(it) < 1e-10) 0.0 else it }
        val sine = sin(radians).let { if (abs(it) < 1e-10) 0.0 else it }
        val newWidth = ceil(width * abs(cosine) + height * abs(sine)).toInt()
        val newHeight = ceil(height * abs(cosine) + width * abs(sine)).toInt()
        val result = ByteArray(pixelCount(newWidth, newHeight, channels))
        for (y in 0 until newHeight) {
            for (x in 0 until newWidth) {
                val dx = x + 0.5 - newWidth / 2.0
                val dy = y + 0.5 - newHeight / 2.0
                val sx = floor(cosine * dx + sine * dy + width / 2.0).toInt()
                val sy = floor(-sine * dx + cosine * dy + height / 2.0).toInt()
                if (sx in 0 until width && sy in 0 until height) {
                    val source = (sy * width + sx) * channels
                    pixels.copyInto(result, (y * newWidth + x) * channels, source, source + channels)
                }
            }
        }
        return LiteRtImage(result, newWidth, newHeight, channels)
    }

    fun flip(horizontal: Boolean, vertical: Boolean): LiteRtImage {
        val result = ByteArray(pixels.size)
        for (y in 0 until height) {
            for (x in 0 until width) {
                val sourceX = if (horizontal) width - 1 - x else x
                val sourceY = if (vertical) height - 1 - y else y
                val source = (sourceY * width + sourceX) * channels
                pixels.copyInto(result, (y * width + x) * channels, source, source + channels)
            }
        }
        return LiteRtImage(result, width, height, channels)
    }

    fun toGrayscale(): LiteRtImage {
        if (channels == 1) return this
        val result = ByteArray(width * height)
        for (i in result.indices) {
            val offset = i * channels
            val red = pixels[offset].toInt() and 255
            val green = pixels[offset + 1].toInt() and 255
            val blue = pixels[offset + 2].toInt() and 255
            result[i] = (0.299 * red + 0.587 * green + 0.114 * blue).roundToInt().toByte()
        }
        return LiteRtImage(result, width, height, 1)
    }

    fun toRgb(): LiteRtImage {
        if (channels == 3) return this
        val result = ByteArray(pixelCount(width, height, 3))
        for (i in 0 until width * height) {
            for (c in 0..2) result[i * 3 + c] = pixels[i * channels + if (channels == 1) 0 else c]
        }
        return LiteRtImage(result, width, height, 3)
    }

    fun toFloatArray(mean: Float = 0f, std: Float = 1f): FloatArray {
        require(std.isFinite() && std != 0f && mean.isFinite()) { "Normalization parameters must be finite and std non-zero" }
        return FloatArray(pixels.size) { ((pixels[it].toInt() and 255) - mean) / std }
    }

    fun toInt8Array(): ByteArray = data
    fun toIntArray(): IntArray = IntArray(pixels.size) { pixels[it].toInt() and 255 }
    fun toBooleanArray(): BooleanArray = BooleanArray(pixels.size) { (pixels[it].toInt() and 255) > 127 }
    fun toLongArray(): LongArray = LongArray(pixels.size) { (pixels[it].toInt() and 255).toLong() }

    fun writeInt8Buffer(buffer: TFBuffer) = buffer.writeInt8(data)
    fun writeFloatBuffer(buffer: TFBuffer, mean: Float = 0f, std: Float = 1f) =
        buffer.writeFloat(toFloatArray(mean, std))

    companion object {
        fun fromBytes(bytes: ByteArray): LiteRtImage = LiteRtImage(bytes)

        fun fromRawRgb(data: ByteArray, width: Int, height: Int): LiteRtImage =
            fromPixels(data, width, height, 3)

        /**
         * Converts a platform frame to an image, then rotates clockwise and applies the flip.
         * Supported frame types vary by platform; see the image processing documentation.
         */
        fun fromVideoFrame(
            frame: Any,
            rotation: ImageRotation = ImageRotation.ROTATION_0,
            flip: ImageFlip = ImageFlip(),
        ): LiteRtImage = decodeVideoFrame(frame).transformFrame(rotation, flip)

        internal fun fromPixels(data: ByteArray, width: Int, height: Int, channels: Int): LiteRtImage {
            require(channels == 1 || channels == 3 || channels == 4) { "Unsupported channel count: $channels" }
            require(data.size == pixelCount(width, height, channels)) { "Pixel data size does not match image dimensions" }
            return LiteRtImage(data.copyOf(), width, height, channels)
        }
    }
}

internal fun LiteRtImage.transformFrame(rotation: ImageRotation, flip: ImageFlip): LiteRtImage {
    val rotated = if (rotation == ImageRotation.ROTATION_0) this else rotate(rotation.degrees.toFloat())
    return if (flip.horizontal || flip.vertical) rotated.flip(flip.horizontal, flip.vertical) else rotated
}

internal expect fun decodeVideoFrame(frame: Any): LiteRtImage

fun pixelCount(width: Int, height: Int, channels: Int): Int {
    require(width > 0 && height > 0 && channels > 0) {
        "Image dimensions and channels must be positive"
    }
    require(width <= Int.MAX_VALUE / height && width * height <= Int.MAX_VALUE / channels) {
        "Image is too large"
    }
    return width * height * channels
}
