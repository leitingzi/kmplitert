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

    actual companion object {
        actual fun fromBytes(bytes: ByteArray): LiteRtImage {
            val bufferedImage = ImageIO.read(ByteArrayInputStream(bytes))
                ?: throw IllegalArgumentException("Failed to decode bufferedImage from bytes")
            return LiteRtImage(bufferedImage)
        }
    }
}


