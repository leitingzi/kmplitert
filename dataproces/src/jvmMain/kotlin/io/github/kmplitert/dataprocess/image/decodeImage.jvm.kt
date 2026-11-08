package io.github.kmplitert.dataprocess.image

import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

actual fun decodeImage(bytes: ByteArray): LiteRtImage {
    val image = ImageIO.read(ByteArrayInputStream(bytes))
        ?: throw IllegalArgumentException("Unable to decode image bytes")
    return LiteRtImage.fromBufferedImage(image)
}

fun LiteRtImage.Companion.fromBufferedImage(image: java.awt.image.BufferedImage): LiteRtImage {
    val width = image.width
    val height = image.height
    val channels = if (image.colorModel.hasAlpha()) 4 else 3
    val pixels = ByteArray(pixelCount(width, height, channels))
    for (y in 0 until height) {
        for (x in 0 until width) {
            val color = image.getRGB(x, y)
            val offset = (y * width + x) * channels
            pixels[offset] = (color shr 16).toByte()
            pixels[offset + 1] = (color shr 8).toByte()
            pixels[offset + 2] = color.toByte()
            if (channels == 4) pixels[offset + 3] = (color ushr 24).toByte()
        }
    }
    return LiteRtImage.fromPixels(pixels, width, height, channels)
}
