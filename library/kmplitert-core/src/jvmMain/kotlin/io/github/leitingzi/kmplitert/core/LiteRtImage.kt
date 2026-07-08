@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.leitingzi.kmplitert.core

import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

actual class LiteRtImage(private val bufferedImage: BufferedImage) {
    actual fun resize(width: Int, height: Int): LiteRtImage {
        val resizedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val graphics2D = resizedImage.createGraphics()
        graphics2D.drawImage(bufferedImage, 0, 0, width, height, null)
        graphics2D.dispose()
        return LiteRtImage(resizedImage)
    }

    actual fun toFloatArray(mean: Float, std: Float): FloatArray {
        val width = bufferedImage.width
        val height = bufferedImage.height
        val floatArray = FloatArray(width * height * 3)
        var index = 0
        for (y in 0 until height) {
            for (x in 0 until width) {
                val rgb = bufferedImage.getRGB(x, y)
                val r = (rgb shr 16 and 0xFF).toFloat()
                val g = (rgb shr 8 and 0xFF).toFloat()
                val b = (rgb and 0xFF).toFloat()
                floatArray[index++] = (r - mean) / std
                floatArray[index++] = (g - mean) / std
                floatArray[index++] = (b - mean) / std
            }
        }
        return floatArray
    }

    actual fun toInt8Array(): ByteArray {
        val width = bufferedImage.width
        val height = bufferedImage.height
        val byteArray = ByteArray(width * height * 3)
        var index = 0
        for (y in 0 until height) {
            for (x in 0 until width) {
                val rgb = bufferedImage.getRGB(x, y)
                byteArray[index++] = (rgb shr 16 and 0xFF).toByte()
                byteArray[index++] = (rgb shr 8 and 0xFF).toByte()
                byteArray[index++] = (rgb and 0xFF).toByte()
            }
        }
        return byteArray
    }

    actual fun toIntArray(): IntArray {
        val width = bufferedImage.width
        val height = bufferedImage.height
        val intArray = IntArray(width * height * 3)
        var index = 0
        for (y in 0 until height) {
            for (x in 0 until width) {
                val rgb = bufferedImage.getRGB(x, y)
                intArray[index++] = (rgb shr 16 and 0xFF)
                intArray[index++] = (rgb shr 8 and 0xFF)
                intArray[index++] = (rgb and 0xFF)
            }
        }
        return intArray
    }

    actual fun toBooleanArray(): BooleanArray {
        val width = bufferedImage.width
        val height = bufferedImage.height
        val booleanArray = BooleanArray(width * height * 3)
        var index = 0
        for (y in 0 until height) {
            for (x in 0 until width) {
                val rgb = bufferedImage.getRGB(x, y)
                booleanArray[index++] = (rgb shr 16 and 0xFF) > 127
                booleanArray[index++] = (rgb shr 8 and 0xFF) > 127
                booleanArray[index++] = (rgb and 0xFF) > 127
            }
        }
        return booleanArray
    }

    actual fun toLongArray(): LongArray {
        val width = bufferedImage.width
        val height = bufferedImage.height
        val longArray = LongArray(width * height * 3)
        var index = 0
        for (y in 0 until height) {
            for (x in 0 until width) {
                val rgb = bufferedImage.getRGB(x, y)
                longArray[index++] = (rgb shr 16 and 0xFF).toLong()
                longArray[index++] = (rgb shr 8 and 0xFF).toLong()
                longArray[index++] = (rgb and 0xFF).toLong()
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


