package io.github.kmplitert.dataprocess.image

import android.graphics.BitmapFactory

actual fun decodeImage(bytes: ByteArray): LiteRtImage {
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        ?: throw IllegalArgumentException("Unable to decode image bytes")
    return bitmap.asLiteRtImage()
}

fun android.graphics.Bitmap.asLiteRtImage(): LiteRtImage {
    val bitmap = this
    val width = bitmap.width
    val height = bitmap.height
    val channels = if (bitmap.hasAlpha()) 4 else 3
    val colors = IntArray(pixelCount(width, height, 1))
    val pixels = ByteArray(pixelCount(width, height, channels))
    bitmap.getPixels(colors, 0, width, 0, 0, width, height)
    for (i in colors.indices) {
        val color = colors[i]
        val offset = i * channels
        pixels[offset] = (color shr 16).toByte()
        pixels[offset + 1] = (color shr 8).toByte()
        pixels[offset + 2] = color.toByte()
        if (channels == 4) pixels[offset + 3] = (color ushr 24).toByte()
    }
    return LiteRtImage.fromPixels(pixels, width, height, channels)
}
