@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.leitingzi

import android.graphics.Bitmap
import android.graphics.BitmapFactory

actual class LiteRtImage(private val bitmap: Bitmap) {
    actual fun resize(width: Int, height: Int): LiteRtImage {
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)
        return LiteRtImage(scaledBitmap)
    }

    actual fun toFloatArray(mean: Float, std: Float): FloatArray {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val floatArray = FloatArray(width * height * 3)
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val r = ((pixel shr 16 and 0xFF) - mean) / std
            val g = ((pixel shr 8 and 0xFF) - mean) / std
            val b = ((pixel and 0xFF) - mean) / std
            floatArray[i * 3] = r
            floatArray[i * 3 + 1] = g
            floatArray[i * 3 + 2] = b
        }
        return floatArray
    }

    actual companion object {
        actual fun fromBytes(bytes: ByteArray): LiteRtImage {
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                ?: throw IllegalArgumentException("Failed to decode bitmap from bytes")
            return LiteRtImage(bitmap)
        }
    }
}
