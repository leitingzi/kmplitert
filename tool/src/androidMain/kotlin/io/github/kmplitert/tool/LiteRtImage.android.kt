@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.kmplitert.tool

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Paint
import android.media.Image
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import io.github.kmplitert.core.TFBuffer

actual class LiteRtImage(internal val bitmap: Bitmap, private val _channels: Int = -1) {
    actual val width: Int get() = bitmap.width
    actual val height: Int get() = bitmap.height
    actual val channels: Int
        get() = if (_channels != -1) _channels else when (bitmap.config) {
            Bitmap.Config.ALPHA_8 -> 1
            Bitmap.Config.RGB_565 -> 3
            Bitmap.Config.ARGB_8888 -> 4
            else -> 4 // Default to 4 for other types
        }

    actual fun resize(width: Int, height: Int): LiteRtImage {
        val scaledBitmap = bitmap.scale(width, height)
        return LiteRtImage(bitmap = scaledBitmap, _channels = _channels)
    }

    actual fun crop(x: Int, y: Int, width: Int, height: Int): LiteRtImage {
        val croppedBitmap = Bitmap.createBitmap(bitmap, x, y, width, height)
        return LiteRtImage(bitmap = croppedBitmap, _channels = _channels)
    }

    actual fun centerCrop(width: Int, height: Int): LiteRtImage {
        val srcWidth = this.width
        val srcHeight = this.height
        val left = (srcWidth - width) / 2
        val top = (srcHeight - height) / 2
        return crop(left.coerceAtLeast(0), top.coerceAtLeast(0), width, height)
    }

    actual fun rotate(degrees: Float): LiteRtImage {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)
        return LiteRtImage(bitmap = rotatedBitmap, _channels = _channels)
    }

    actual fun flip(horizontal: Boolean, vertical: Boolean): LiteRtImage {
        val matrix = Matrix()
        val sx = if (horizontal) -1f else 1f
        val sy = if (vertical) -1f else 1f
        matrix.postScale(sx, sy, width / 2f, height / 2f)
        val flippedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)
        return LiteRtImage(flippedBitmap, _channels)
    }

    actual fun toGrayscale(): LiteRtImage {
        val bmpGrayscale = createBitmap(width, height, Bitmap.Config.ALPHA_8)
        val canvas = Canvas(bmpGrayscale)
        val paint = Paint()
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f)
        val filter = ColorMatrixColorFilter(colorMatrix)
        paint.colorFilter = filter
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return LiteRtImage(bmpGrayscale, 1)
    }

    actual fun toRgb(): LiteRtImage {
        if (channels == 3 && bitmap.config == Bitmap.Config.RGB_565) return this
        return LiteRtImage(bitmap, 3)
    }

    actual fun toFloatArray(mean: Float, std: Float): FloatArray {
        val width = bitmap.width
        val height = bitmap.height
        val c = channels
        val pixels = IntArray(size = width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val floatArray = FloatArray(size = width * height * c)
        for (i in pixels.indices) {
            val pixel = pixels[i]
            if (c >= 3) {
                floatArray[i * c] = ((pixel shr 16 and 0xFF) - mean) / std
                floatArray[i * c + 1] = ((pixel shr 8 and 0xFF) - mean) / std
                floatArray[i * c + 2] = ((pixel and 0xFF) - mean) / std
                if (c == 4) {
                    floatArray[i * c + 3] = ((pixel shr 24 and 0xFF) - mean) / std
                }
            } else if (c == 1) {
                // For grayscale, we take one channel (usually Alpha or calculated Gray)
                // If it's ALPHA_8, the value is in the alpha channel or we can use the bits.
                // bitmap.getPixels for ALPHA_8 usually returns the alpha in bits 24-31.
                floatArray[i] = ((pixel shr 24 and 0xFF) - mean) / std
            }
        }
        return floatArray
    }

    actual fun toInt8Array(): ByteArray {
        val width = bitmap.width
        val height = bitmap.height
        val c = channels
        val pixels = IntArray(size = width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val byteArray = ByteArray(size = width * height * c)
        for (i in pixels.indices) {
            val pixel = pixels[i]
            if (c >= 3) {
                byteArray[i * c] = (pixel shr 16 and 0xFF).toByte()
                byteArray[i * c + 1] = (pixel shr 8 and 0xFF).toByte()
                byteArray[i * c + 2] = (pixel and 0xFF).toByte()
                if (c == 4) {
                    byteArray[i * c + 3] = (pixel shr 24 and 0xFF).toByte()
                }
            } else if (c == 1) {
                byteArray[i] = (pixel shr 24 and 0xFF).toByte()
            }
        }
        return byteArray
    }

    actual fun toIntArray(): IntArray {
        val width = bitmap.width
        val height = bitmap.height
        val c = channels
        val pixels = IntArray(size = width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val intArray = IntArray(size = width * height * c)
        for (i in pixels.indices) {
            val pixel = pixels[i]
            if (c >= 3) {
                intArray[i * c] = (pixel shr 16 and 0xFF)
                intArray[i * c + 1] = (pixel shr 8 and 0xFF)
                intArray[i * c + 2] = (pixel and 0xFF)
                if (c == 4) {
                    intArray[i * c + 3] = (pixel shr 24 and 0xFF)
                }
            } else if (c == 1) {
                intArray[i] = (pixel shr 24 and 0xFF)
            }
        }
        return intArray
    }

    actual fun toBooleanArray(): BooleanArray {
        val width = bitmap.width
        val height = bitmap.height
        val c = channels
        val pixels = IntArray(size = width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val booleanArray = BooleanArray(size = width * height * c)
        for (i in pixels.indices) {
            val pixel = pixels[i]
            if (c >= 3) {
                booleanArray[i * c] = (pixel shr 16 and 0xFF) > 127
                booleanArray[i * c + 1] = (pixel shr 8 and 0xFF) > 127
                booleanArray[i * c + 2] = (pixel and 0xFF) > 127
                if (c == 4) {
                    booleanArray[i * c + 3] = (pixel shr 24 and 0xFF) > 127
                }
            } else if (c == 1) {
                booleanArray[i] = (pixel shr 24 and 0xFF) > 127
            }
        }
        return booleanArray
    }

    actual fun toLongArray(): LongArray {
        val width = bitmap.width
        val height = bitmap.height
        val c = channels
        val pixels = IntArray(size = width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val longArray = LongArray(size = width * height * c)
        for (i in pixels.indices) {
            val pixel = pixels[i]
            if (c >= 3) {
                longArray[i * c] = (pixel shr 16 and 0xFF).toLong()
                longArray[i * c + 1] = (pixel shr 8 and 0xFF).toLong()
                longArray[i * c + 2] = (pixel and 0xFF).toLong()
                if (c == 4) {
                    longArray[i * c + 3] = (pixel shr 24 and 0xFF).toLong()
                }
            } else if (c == 1) {
                longArray[i] = (pixel shr 24 and 0xFF).toLong()
            }
        }
        return longArray
    }

    actual fun writeInt8Buffer(buffer: TFBuffer) {
        buffer.writeInt8(toInt8Array())
    }

    actual fun writeFloatBuffer(buffer: TFBuffer, mean: Float, std: Float) {
        buffer.writeFloat(toFloatArray(mean, std))
    }

    actual companion object {
        actual fun fromBytes(bytes: ByteArray): LiteRtImage {
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                ?: throw Exception("Failed to decode bitmap from bytes")
            return LiteRtImage(bitmap = bitmap)
        }

        actual fun fromRawRgb(data: ByteArray, width: Int, height: Int): LiteRtImage {
            val bitmap = createBitmap(width, height)
            val pixels = IntArray(size = width * height)
            for (i in 0 until width * height) {
                val r = data[i * 3].toInt() and 0xFF
                val g = data[i * 3 + 1].toInt() and 0xFF
                val b = data[i * 3 + 2].toInt() and 0xFF
                pixels[i] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
            }
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
            return LiteRtImage(bitmap = bitmap)
        }
    }
}

/**
 * Creates a [LiteRtImage] from an Android [Image] (e.g., from CameraX).
 *
 * This function handles YUV_420_888 to RGB conversion efficiently.
 */
fun LiteRtImage.Companion.fromAndroidImage(image: Image): LiteRtImage {
    require(image.format == ImageFormat.YUV_420_888) {
        "Unsupported image format: ${image.format}. Only YUV_420_888 is supported."
    }

    val width = image.width
    val height = image.height
    val planes = image.planes
    
    val yPlane = planes[0]
    val uPlane = planes[1]
    val vPlane = planes[2]

    val yBuffer = yPlane.buffer
    val uBuffer = uPlane.buffer
    val vBuffer = vPlane.buffer

    val yRowStride = yPlane.rowStride
    val uvRowStride = uPlane.rowStride
    val uvPixelStride = uPlane.pixelStride

    val pixels = IntArray(width * height)
    val yData = ByteArray(yBuffer.remaining())
    val uData = ByteArray(uBuffer.remaining())
    val vData = ByteArray(vBuffer.remaining())
    
    yBuffer.get(yData)
    uBuffer.get(uData)
    vBuffer.get(vData)

    for (y in 0 until height) {
        for (x in 0 until width) {
            val yIndex = y * yRowStride + x
            val uvIndex = (y / 2) * uvRowStride + (x / 2) * uvPixelStride

            val yValue = yData[yIndex].toInt() and 0xFF
            val uValue = (uData[uvIndex.coerceAtMost(uData.size - 1)].toInt() and 0xFF) - 128
            val vValue = (vData[uvIndex.coerceAtMost(vData.size - 1)].toInt() and 0xFF) - 128

            val r = (yValue + 1.370705f * vValue).toInt().coerceIn(0, 255)
            val g = (yValue - 0.337633f * uValue - 0.698001f * vValue).toInt().coerceIn(0, 255)
            val b = (yValue + 1.732446f * uValue).toInt().coerceIn(0, 255)

            pixels[y * width + x] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
        }
    }

    val bitmap = createBitmap(width, height)
    bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
    return LiteRtImage(bitmap)
}

fun LiteRtImage.asBitmap(): Bitmap {
    return this.bitmap
}

fun Bitmap.asLiteRtImage(): LiteRtImage {
    return LiteRtImage(this)
}
