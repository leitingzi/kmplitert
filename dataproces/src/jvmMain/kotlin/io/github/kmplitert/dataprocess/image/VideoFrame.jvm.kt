package io.github.kmplitert.dataprocess.image

import java.awt.image.BufferedImage
import java.nio.ByteBuffer

internal actual fun decodeVideoFrame(frame: Any): LiteRtImage = when (frame) {
    is BufferedImage -> LiteRtImage.fromBufferedImage(frame)
    is ByteBuffer -> throw IllegalArgumentException("YUV frame dimensions are required; use fromYuvByteBuffer")
    else -> throw IllegalArgumentException("Unsupported frame type on JVM: ${frame.javaClass.name}")
}

fun LiteRtImage.Companion.fromYuvByteBuffer(
    buffer: ByteBuffer,
    width: Int,
    height: Int,
    isNV21: Boolean = true,
    rotation: ImageRotation = ImageRotation.ROTATION_0,
    flip: ImageFlip = ImageFlip(),
): LiteRtImage {
    val size = pixelCount(width, height, 3)
    require(width % 2 == 0 && height % 2 == 0) { "NV12/NV21 requires even dimensions" }
    val ySize = width * height
    require(buffer.remaining().toLong() >= ySize.toLong() * 3 / 2) { "Truncated YUV frame" }
    val data = buffer.slice()
    val pixels = ByteArray(size)
    for (y in 0 until height) {
        for (x in 0 until width) {
            val uv = ySize + (y / 2) * width + (x / 2) * 2
            val first = data.get(uv).toInt() and 255
            val second = data.get(uv + 1).toInt() and 255
            val rgb = yuvToRgb(data.get(y * width + x).toInt() and 255,
                if (isNV21) second else first, if (isNV21) first else second)
            val offset = (y * width + x) * 3
            pixels[offset] = (rgb shr 16).toByte()
            pixels[offset + 1] = (rgb shr 8).toByte()
            pixels[offset + 2] = rgb.toByte()
        }
    }
    return LiteRtImage.fromPixels(pixels, width, height, 3).transformFrame(rotation, flip)
}

fun BufferedImage.asLiteRtImage(): LiteRtImage = LiteRtImage.fromBufferedImage(this)

fun LiteRtImage.asBufferedImage(): BufferedImage {
    val image = BufferedImage(width, height, if (channels == 4) BufferedImage.TYPE_INT_ARGB else BufferedImage.TYPE_INT_RGB)
    val pixels = data
    for (y in 0 until height) for (x in 0 until width) {
        val offset = (y * width + x) * channels
        val r = pixels[offset].toInt() and 255
        val g = pixels[offset + if (channels == 1) 0 else 1].toInt() and 255
        val b = pixels[offset + if (channels == 1) 0 else 2].toInt() and 255
        val a = if (channels == 4) pixels[offset + 3].toInt() and 255 else 255
        image.setRGB(x, y, (a shl 24) or (r shl 16) or (g shl 8) or b)
    }
    return image
}
