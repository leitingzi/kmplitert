@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.leitingzi.kmplitert.tool

import java.awt.Graphics2D
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
        // getSubimage returns a shared image, we might want a copy to be safe, but for now this is fine.
        // Actually, for consistency with other platforms, a copy might be better.
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
                    // For grayscale, BufferedImage usually returns the gray value in R, G, and B if it's TYPE_BYTE_GRAY
                    // or we can just take one.
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
    }
}

fun LiteRtImage.asBufferedImage(): BufferedImage {
    return this.bufferedImage
}

fun BufferedImage.asLiteRtImage(): LiteRtImage {
    return LiteRtImage(this)
}
