@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.kmplitert.tool

import io.github.kmplitert.core.TFBuffer
import java.awt.RenderingHints
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

actual class LiteRtImage(internal val bufferedImage: BufferedImage, private val _channels: Int = -1) {
    actual val width: Int get() = bufferedImage.width
    actual val height: Int get() = bufferedImage.height
    actual val channels: Int 
        get() = if (_channels != -1) _channels else bufferedImage.colorModel.numComponents

    actual fun resize(width: Int, height: Int): LiteRtImage {
        val resizedImage = BufferedImage(width, height, bufferedImage.type.let { if (it == 0) BufferedImage.TYPE_INT_ARGB else it })
        val graphics2D = resizedImage.createGraphics()
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        graphics2D.drawImage(bufferedImage, 0, 0, width, height, null)
        graphics2D.dispose()
        return LiteRtImage(resizedImage, _channels)
    }

    actual fun crop(x: Int, y: Int, width: Int, height: Int): LiteRtImage {
        val croppedImage = bufferedImage.getSubimage(x, y, width, height)
        val copy = BufferedImage(width, height, bufferedImage.type.let { if (it == 0) BufferedImage.TYPE_INT_ARGB else it })
        val g = copy.createGraphics()
        g.drawImage(croppedImage, 0, 0, null)
        g.dispose()
        return LiteRtImage(copy, _channels)
    }

    actual fun centerCrop(width: Int, height: Int): LiteRtImage {
        val left = (this.width - width) / 2
        val top = (this.height - height) / 2
        return crop(left.coerceAtLeast(0), top.coerceAtLeast(0), width, height)
    }

    actual fun rotate(degrees: Float): LiteRtImage {
        val rads = Math.toRadians(degrees.toDouble())
        val sin = abs(sin(rads))
        val cos = abs(cos(rads))
        val w = width
        val h = height
        val newWidth = floor(w * cos + h * sin).toInt()
        val newHeight = floor(h * cos + w * sin).toInt()

        val rotatedImage = BufferedImage(newWidth, newHeight, bufferedImage.type.let { if (it == 0) BufferedImage.TYPE_INT_ARGB else it })
        val g = rotatedImage.createGraphics()
        g.translate((newWidth - w) / 2, (newHeight - h) / 2)
        g.rotate(rads, w / 2.0, h / 2.0)
        g.drawRenderedImage(bufferedImage, null)
        g.dispose()
        return LiteRtImage(rotatedImage, _channels)
    }

    actual fun flip(horizontal: Boolean, vertical: Boolean): LiteRtImage {
        val flippedImage = BufferedImage(width, height, bufferedImage.type.let { if (it == 0) BufferedImage.TYPE_INT_ARGB else it })
        val g = flippedImage.createGraphics()
        val tx = AffineTransform.getScaleInstance(if (horizontal) -1.0 else 1.0, if (vertical) -1.0 else 1.0)
        if (horizontal) tx.translate(-width.toDouble(), 0.0)
        if (vertical) tx.translate(0.0, -height.toDouble())
        g.drawImage(bufferedImage, tx, null)
        g.dispose()
        return LiteRtImage(flippedImage, _channels)
    }

    actual fun toGrayscale(): LiteRtImage {
        val grayImage = BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY)
        val g = grayImage.createGraphics()
        g.drawImage(bufferedImage, 0, 0, null)
        g.dispose()
        return LiteRtImage(grayImage, 1)
    }

    actual fun toRgb(): LiteRtImage {
        if (channels == 3 && bufferedImage.type == BufferedImage.TYPE_INT_RGB) return this
        return LiteRtImage(bufferedImage, 3)
    }

    actual fun toFloatArray(mean: Float, std: Float): FloatArray {
        val width = bufferedImage.width
        val height = bufferedImage.height
        val c = channels
        val floatArray = FloatArray(width * height * c)
        var index = 0
        for (y in 0 until height) {
            for (x in 0 until width) {
                val rgb = bufferedImage.getRGB(x, y)
                if (c >= 3) {
                    floatArray[index++] = ((rgb shr 16 and 0xFF).toFloat() - mean) / std
                    floatArray[index++] = ((rgb shr 8 and 0xFF).toFloat() - mean) / std
                    floatArray[index++] = ((rgb and 0xFF).toFloat() - mean) / std
                    if (c == 4) {
                        floatArray[index++] = ((rgb shr 24 and 0xFF).toFloat() - mean) / std
                    }
                } else if (c == 1) {
                    floatArray[index++] = ((rgb and 0xFF).toFloat() - mean) / std
                }
            }
        }
        return floatArray
    }

    actual fun toInt8Array(): ByteArray {
        val width = bufferedImage.width
        val height = bufferedImage.height
        val c = channels
        val byteArray = ByteArray(width * height * c)
        var index = 0
        for (y in 0 until height) {
            for (x in 0 until width) {
                val rgb = bufferedImage.getRGB(x, y)
                if (c >= 3) {
                    byteArray[index++] = (rgb shr 16 and 0xFF).toByte()
                    byteArray[index++] = (rgb shr 8 and 0xFF).toByte()
                    byteArray[index++] = (rgb and 0xFF).toByte()
                    if (c == 4) {
                        byteArray[index++] = (rgb shr 24 and 0xFF).toByte()
                    }
                } else if (c == 1) {
                    byteArray[index++] = (rgb and 0xFF).toByte()
                }
            }
        }
        return byteArray
    }

    actual fun toIntArray(): IntArray {
        val width = bufferedImage.width
        val height = bufferedImage.height
        val c = channels
        val intArray = IntArray(width * height * c)
        var index = 0
        for (y in 0 until height) {
            for (x in 0 until width) {
                val rgb = bufferedImage.getRGB(x, y)
                if (c >= 3) {
                    intArray[index++] = (rgb shr 16 and 0xFF)
                    intArray[index++] = (rgb shr 8 and 0xFF)
                    intArray[index++] = (rgb and 0xFF)
                    if (c == 4) {
                        intArray[index++] = (rgb shr 24 and 0xFF)
                    }
                } else if (c == 1) {
                    intArray[index++] = (rgb and 0xFF)
                }
            }
        }
        return intArray
    }

    actual fun toBooleanArray(): BooleanArray {
        val width = bufferedImage.width
        val height = bufferedImage.height
        val c = channels
        val booleanArray = BooleanArray(width * height * c)
        var index = 0
        for (y in 0 until height) {
            for (x in 0 until width) {
                val rgb = bufferedImage.getRGB(x, y)
                if (c >= 3) {
                    booleanArray[index++] = (rgb shr 16 and 0xFF) > 127
                    booleanArray[index++] = (rgb shr 8 and 0xFF) > 127
                    booleanArray[index++] = (rgb and 0xFF) > 127
                    if (c == 4) {
                        booleanArray[index++] = (rgb shr 24 and 0xFF) > 127
                    }
                } else if (c == 1) {
                    booleanArray[index++] = (rgb and 0xFF) > 127
                }
            }
        }
        return booleanArray
    }

    actual fun toLongArray(): LongArray {
        val width = bufferedImage.width
        val height = bufferedImage.height
        val c = channels
        val longArray = LongArray(width * height * c)
        var index = 0
        for (y in 0 until height) {
            for (x in 0 until width) {
                val rgb = bufferedImage.getRGB(x, y)
                if (c >= 3) {
                    longArray[index++] = (rgb shr 16 and 0xFF).toLong()
                    longArray[index++] = (rgb shr 8 and 0xFF).toLong()
                    longArray[index++] = (rgb and 0xFF).toLong()
                    if (c == 4) {
                        longArray[index++] = (rgb shr 24 and 0xFF).toLong()
                    }
                } else if (c == 1) {
                    longArray[index++] = (rgb and 0xFF).toLong()
                }
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
            val bufferedImage = ImageIO.read(ByteArrayInputStream(bytes))
                ?: throw IllegalArgumentException("Failed to decode bufferedImage from bytes")
            return LiteRtImage(bufferedImage)
        }

        actual fun fromRawRgb(data: ByteArray, width: Int, height: Int): LiteRtImage {
            val bufferedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val i = y * width + x
                    val r = data[i * 3].toInt() and 0xFF
                    val g = data[i * 3 + 1].toInt() and 0xFF
                    val b = data[i * 3 + 2].toInt() and 0xFF
                    val rgb = (r shl 16) or (g shl 8) or b
                    bufferedImage.setRGB(x, y, rgb)
                }
            }
            return LiteRtImage(bufferedImage)
        }

        actual fun fromVideoFrame(
            frame: Any,
            rotation: LiteRtRotation,
            flip: LiteRtFlip
        ): LiteRtImage {
            return when (frame) {
                is BufferedImage -> {
                    var image = LiteRtImage(frame)
                    if (rotation != LiteRtRotation.ROTATION_0) image = image.rotate(rotation.degrees.toFloat())
                    if (flip.horizontal || flip.vertical) image = image.flip(flip.horizontal, flip.vertical)
                    image
                }
                is java.nio.ByteBuffer -> {
                    fromYuvByteBuffer(frame, 640, 480, true, rotation, flip)
                }
                else -> throw IllegalArgumentException("Unsupported frame type on JVM: ${frame.javaClass.name}")
            }
        }

        /**
         * Creates a [LiteRtImage] from a [java.awt.image.BufferedImage].
         */
        fun fromBufferedImage(bufferedImage: BufferedImage): LiteRtImage {
            return LiteRtImage(bufferedImage)
        }

        /**
         * Creates a [LiteRtImage] from a raw YUV/NV21 or NV12 [java.nio.ByteBuffer].
         * 
         * @param buffer The YUV data buffer.
         * @param width The image width.
         * @param height The image height.
         * @param isNV21 True if the format is NV21 (V then U), false if NV12 (U then V).
         */
        fun fromYuvByteBuffer(
            buffer: java.nio.ByteBuffer,
            width: Int,
            height: Int,
            isNV21: Boolean = true,
            rotation: LiteRtRotation = LiteRtRotation.ROTATION_0,
            flip: LiteRtFlip = LiteRtFlip()
        ): LiteRtImage {
            val data = ByteArray(buffer.remaining())
            buffer.get(data)
            
            val pixels = IntArray(width * height)
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val yIndex = y * width + x
                    val uvIndex = width * height + (y / 2) * width + (x / 2) * 2
                    
                    val yValue = data[yIndex].toInt() and 0xFF
                    
                    val uValue: Int
                    val vValue: Int
                    if (isNV21) {
                        vValue = (data[uvIndex].toInt() and 0xFF) - 128
                        uValue = (data[uvIndex + 1].toInt() and 0xFF) - 128
                    } else {
                        uValue = (data[uvIndex].toInt() and 0xFF) - 128
                        vValue = (data[uvIndex + 1].toInt() and 0xFF) - 128
                    }
                    
                    val r = (yValue + 1.370705f * vValue).toInt().coerceIn(0, 255)
                    val g = (yValue - 0.337633f * uValue - 0.698001f * vValue).toInt().coerceIn(0, 255)
                    val b = (yValue + 1.732446f * uValue).toInt().coerceIn(0, 255)
                    
                    pixels[y * width + x] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
                }
            }
            
            val bufferedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
            bufferedImage.setRGB(0, 0, width, height, pixels, 0, width)
            
            var image = LiteRtImage(bufferedImage)
            if (rotation != LiteRtRotation.ROTATION_0) image = image.rotate(rotation.degrees.toFloat())
            if (flip.horizontal || flip.vertical) image = image.flip(flip.horizontal, flip.vertical)
            
            return image
        }
    }
}

fun LiteRtImage.asBufferedImage(): BufferedImage {
    return this.bufferedImage
}

fun BufferedImage.asLiteRtImage(): LiteRtImage {
    return LiteRtImage(this)
}
