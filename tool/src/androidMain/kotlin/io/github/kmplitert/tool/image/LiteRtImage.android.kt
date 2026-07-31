@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.kmplitert.tool.image

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
import java.nio.ByteBuffer

actual class LiteRtImage(
    internal val bitmap: Bitmap,
    private val _channels: Int = -1
) {
    actual val width: Int
        get() = bitmap.width

    actual val height: Int
        get() = bitmap.height

    actual val channels: Int
        get() = if (_channels != -1) {
            _channels
        } else {
            when (bitmap.config) {
                Bitmap.Config.ALPHA_8 -> 1
                Bitmap.Config.RGB_565 -> 3
                Bitmap.Config.ARGB_8888 -> 4
                else -> 4 // Default to 4 for other types
            }
        }

    actual fun resize(width: Int, height: Int): LiteRtImage {
        val scaledBitmap = bitmap.scale(width = width, height = height)
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

        val cropX = left.coerceAtLeast(0)
        val cropY = top.coerceAtLeast(0)

        return crop(x = cropX, y = cropY, width = width, height = height)
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
        return LiteRtImage(bitmap = flippedBitmap, _channels = _channels)
    }

    actual fun toGrayscale(): LiteRtImage {
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f)

        val filter = ColorMatrixColorFilter(colorMatrix)
        val paint = Paint()
        paint.colorFilter = filter

        val bmpGrayscale = createBitmap(width, height, Bitmap.Config.ALPHA_8)
        val canvas = Canvas(bmpGrayscale)

        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return LiteRtImage(bitmap = bmpGrayscale, _channels = 1)
    }

    actual fun toRgb(): LiteRtImage {
        if (channels == 3 && bitmap.config == Bitmap.Config.RGB_565) {
            return this
        }
        return LiteRtImage(bitmap = bitmap, _channels = 3)
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
            bitmap ?: throw Exception("Failed to decode bitmap from bytes")

            return LiteRtImage(bitmap = bitmap)
        }

        actual fun fromRawRgb(data: ByteArray, width: Int, height: Int): LiteRtImage {
            val bitmap = createBitmap(width = width, height = height)
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

        actual fun fromVideoFrame(frame: Any, rotation: ImageRotation, flip: ImageFlip): LiteRtImage {
            return when (frame) {
                is Image -> {
                    fromAndroidImage(image = frame, rotation = rotation, flip = flip)
                }
                is Bitmap -> {
                    var image = LiteRtImage(bitmap = frame)
                    if (rotation != ImageRotation.ROTATION_0) {
                        image = image.rotate(rotation.degrees.toFloat())
                    }
                    if (flip.horizontal || flip.vertical) {
                        image = image.flip(flip.horizontal, flip.vertical)
                    }
                    image
                }
                else -> {
                    // Try to handle ImageProxy via reflection to avoid direct dependency
                    try {
                        val imageMethod = frame.javaClass.getMethod("getImage")
                        val image = imageMethod.invoke(frame) as? Image
                        if (image != null) {
                            return fromAndroidImage(image, rotation, flip)
                        }
                    } catch (e: Exception) {
                        // Not an ImageProxy or reflection failed
                    }
                    throw IllegalArgumentException("Unsupported frame type: ${frame.javaClass.name}")
                }
            }
        }
    }
}

/**
 * Creates a [LiteRtImage] from an Android [Image] (e.g., from CameraX) with optional transformation.
 *
 * This function handles YUV_420_888 to RGB conversion efficiently using native C++ code.
 */
fun LiteRtImage.Companion.fromAndroidImage(
    image: Image,
    rotation: ImageRotation = ImageRotation.ROTATION_0,
    flip: ImageFlip = ImageFlip()
): LiteRtImage {
    require(value = image.format == ImageFormat.YUV_420_888) {
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

    // Target dimensions after rotation
    val targetWidth = if (rotation.isLandscapeRotation) height else width
    val targetHeight = if (rotation.isLandscapeRotation) width else height
    
    val bitmap = createBitmap(
        width = targetWidth,
        height = targetHeight,
        config = Bitmap.Config.ARGB_8888
    )
    
    if (isNativeLibraryLoaded && yBuffer.isDirect && uBuffer.isDirect && vBuffer.isDirect) {
        nativeConvertYUV(
            yBuffer = yBuffer,
            yRowStride = yPlane.rowStride,
            uBuffer = uBuffer,
            vBuffer = vBuffer,
            uvRowStride = uPlane.rowStride,
            uvPixelStride = uPlane.pixelStride,
            outBitmap = bitmap,
            width = width,
            height = height,
            rotationDeg = rotation.degrees,
            flipH = flip.horizontal,
            flipV = flip.vertical
        )
    } else {
        // Fallback to Kotlin implementation
        val pixels = IntArray(size = targetWidth * targetHeight)
        val yRowStride = yPlane.rowStride
        val uvRowStride = uPlane.rowStride
        val uvPixelStride = uPlane.pixelStride
        
        val yLimit = yBuffer.limit()
        val uLimit = uBuffer.limit()
        val vLimit = vBuffer.limit()
        
        for (y in 0 until height) {
            for (x in 0 until width) {
                val yIndex = y * yRowStride + x
                val uvIndex = (y / 2) * uvRowStride + (x / 2) * uvPixelStride

                val yValue = (if (yIndex < yLimit) yBuffer.get(yIndex).toInt() else 0) and 0xFF
                val uValue = ((if (uvIndex < uLimit) uBuffer.get(uvIndex).toInt() else 0) and 0xFF) - 128
                val vValue = ((if (uvIndex < vLimit) vBuffer.get(uvIndex).toInt() else 0) and 0xFF) - 128

                val r = (yValue + 1.370705f * vValue).toInt().coerceIn(0, 255)
                val g = (yValue - 0.337633f * uValue - 0.698001f * vValue).toInt().coerceIn(0, 255)
                val b = (yValue + 1.732446f * uValue).toInt().coerceIn(0, 255)
                val rgba = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
                
                // Handle rotation/flip in Kotlin fallback
                var tx = x; var ty = y
                if (flip.horizontal) tx = width - 1 - tx
                if (flip.vertical) ty = height - 1 - ty
                
                val (fx, fy) = when(rotation) {
                    ImageRotation.ROTATION_90 -> (targetWidth - 1 - ty) to tx
                    ImageRotation.ROTATION_180 -> (targetWidth - 1 - tx) to (targetHeight - 1 - ty)
                    ImageRotation.ROTATION_270 -> ty to (targetHeight - 1 - tx)
                    else -> tx to ty
                }
                pixels[fy * targetWidth + fx] = rgba
            }
        }
        bitmap.setPixels(pixels, 0, targetWidth, 0, 0, targetWidth, targetHeight)
    }

    return LiteRtImage(bitmap)
}

private external fun nativeConvertYUV(
    yBuffer: ByteBuffer,
    yRowStride: Int,
    uBuffer: ByteBuffer,
    vBuffer: ByteBuffer,
    uvRowStride: Int,
    uvPixelStride: Int,
    outBitmap: Bitmap,
    width: Int,
    height: Int,
    rotationDeg: Int,
    flipH: Boolean,
    flipV: Boolean
)

private val isNativeLibraryLoaded = try {
    System.loadLibrary("kmplitert_tool_native")
    true
} catch (e: UnsatisfiedLinkError) {
    false
}

fun LiteRtImage.asBitmap(): Bitmap {
    return this.bitmap
}

fun Bitmap.asLiteRtImage(): LiteRtImage {
    return LiteRtImage(bitmap = this)
}
