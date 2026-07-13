@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.leitingzi.kmplitert.core

import android.graphics.Bitmap
import android.graphics.BitmapFactory

actual class LiteRtImage(private val bitmap: Bitmap) {
    actual fun resize(width: Int, height: Int): LiteRtImage {
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)
        return LiteRtImage(bitmap = scaledBitmap)
    }

    actual fun toFloatArray(mean: Float, std: Float): FloatArray {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(size = width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val floatArray = FloatArray(size = width * height * 3)
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

    actual fun toInt8Array(): ByteArray {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(size = width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val byteArray = ByteArray(size = width * height * 3)
        for (i in pixels.indices) {
            val pixel = pixels[i]
            byteArray[i * 3] = (pixel shr 16 and 0xFF).toByte()
            byteArray[i * 3 + 1] = (pixel shr 8 and 0xFF).toByte()
            byteArray[i * 3 + 2] = (pixel and 0xFF).toByte()
        }
        return byteArray
    }

    actual fun toIntArray(): IntArray {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(size = width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val intArray = IntArray(size = width * height * 3)
        for (i in pixels.indices) {
            val pixel = pixels[i]
            intArray[i * 3] = (pixel shr 16 and 0xFF)
            intArray[i * 3 + 1] = (pixel shr 8 and 0xFF)
            intArray[i * 3 + 2] = (pixel and 0xFF)
        }
        return intArray
    }

    actual fun toBooleanArray(): BooleanArray {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(size = width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val booleanArray = BooleanArray(size = width * height * 3)
        for (i in pixels.indices) {
            val pixel = pixels[i]
            booleanArray[i * 3] = (pixel shr 16 and 0xFF) > 127
            booleanArray[i * 3 + 1] = (pixel shr 8 and 0xFF) > 127
            booleanArray[i * 3 + 2] = (pixel and 0xFF) > 127
        }
        return booleanArray
    }

    actual fun toLongArray(): LongArray {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(size = width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val longArray = LongArray(size = width * height * 3)
        for (i in pixels.indices) {
            val pixel = pixels[i]
            longArray[i * 3] = (pixel shr 16 and 0xFF).toLong()
            longArray[i * 3 + 1] = (pixel shr 8 and 0xFF).toLong()
            longArray[i * 3 + 2] = (pixel and 0xFF).toLong()
        }
        return longArray
    }

    actual companion object {
        actual fun fromBytes(bytes: ByteArray): LiteRtImage {
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                ?: throw Exception("Failed to decode bitmap from bytes")
            return LiteRtImage(bitmap = bitmap)
        }

        actual fun fromRawRgb(data: ByteArray, width: Int, height: Int): LiteRtImage {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
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


