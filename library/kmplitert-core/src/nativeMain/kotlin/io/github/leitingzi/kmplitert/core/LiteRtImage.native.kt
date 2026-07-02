@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.leitingzi.kmplitert.core

actual class LiteRtImage(
    val data: ByteArray,
    val width: Int,
    val height: Int
) {
    actual fun resize(width: Int, height: Int): LiteRtImage {
        val newData = ByteArray(width * height * 3)
        val xRatio = if (width > 1) (this.width - 1).toFloat() / (width - 1) else 0f
        val yRatio = if (height > 1) (this.height - 1).toFloat() / (height - 1) else 0f

        for (i in 0 until height) {
            for (j in 0 until width) {
                val x = (xRatio * j).toInt()
                val y = (yRatio * i).toInt()
                val xDiff = (xRatio * j) - x
                val yDiff = (yRatio * i) - y

                val xNext = if (x + 1 < this.width) x + 1 else x
                val yNext = if (y + 1 < this.height) y + 1 else y
                
                for (c in 0 until 3) {
                    val a = this.data[((y * this.width + x) * 3 + c)].toInt() and 0xFF
                    val b = this.data[((y * this.width + xNext) * 3 + c)].toInt() and 0xFF
                    val d = this.data[((yNext * this.width + x) * 3 + c)].toInt() and 0xFF
                    val e = this.data[((yNext * this.width + xNext) * 3 + c)].toInt() and 0xFF

                    // Y = A(1-w)(1-h) + B(w)(1-h) + D(h)(1-w) + E(wh)
                    val pixel = (a * (1 - xDiff) * (1 - yDiff) +
                            b * (xDiff) * (1 - yDiff) +
                            d * (yDiff) * (1 - xDiff) +
                            e * (yDiff * xDiff)).toInt()

                    newData[((i * width + j) * 3 + c)] = pixel.coerceIn(0, 255).toByte()
                }
            }
        }
        return LiteRtImage(newData, width, height)
    }

    actual fun toFloatArray(mean: Float, std: Float): FloatArray {
        val floatArray = FloatArray(width * height * 3)
        for (i in 0 until width * height * 3) {
            val value = data[i].toInt() and 0xFF
            floatArray[i] = (value - mean) / std
        }
        return floatArray
    }

    actual companion object {
        actual fun fromBytes(bytes: ByteArray): LiteRtImage {
            if (bytes.size > 54 && bytes[0] == 'B'.code.toByte() && bytes[1] == 'M'.code.toByte()) {
                return decodeBmp(bytes)
            }
            throw UnsupportedOperationException("Only BMP format is supported in pure Kotlin native implementation for now.")
        }

        private fun decodeBmp(bytes: ByteArray): LiteRtImage {
            // Very basic BMP decoder (supports 24-bit RGB)
            val width = readInt32(bytes, 18)
            val height = readInt32(bytes, 22)
            val offset = readInt32(bytes, 10)
            val bitsPerPixel = readInt16(bytes, 28)

            if (bitsPerPixel != 24) {
                throw UnsupportedOperationException("Only 24-bit BMP is supported. Found $bitsPerPixel bits.")
            }

            val rowSize = (width * 3 + 3) / 4 * 4
            val data = ByteArray(width * height * 3)

            for (y in 0 until height) {
                // BMP stores rows from bottom to top
                val rowOffset = offset + (height - 1 - y) * rowSize
                for (x in 0 until width) {
                    val b = bytes[rowOffset + x * 3]
                    val g = bytes[rowOffset + x * 3 + 1]
                    val r = bytes[rowOffset + x * 3 + 2]
                    
                    val destOffset = (y * width + x) * 3
                    data[destOffset] = r
                    data[destOffset + 1] = g
                    data[destOffset + 2] = b
                }
            }
            return LiteRtImage(data, width, height)
        }

        private fun readInt32(bytes: ByteArray, offset: Int): Int {
            return (bytes[offset].toInt() and 0xFF) or
                    ((bytes[offset + 1].toInt() and 0xFF) shl 8) or
                    ((bytes[offset + 2].toInt() and 0xFF) shl 16) or
                    ((bytes[offset + 3].toInt() and 0xFF) shl 24)
        }

        private fun readInt16(bytes: ByteArray, offset: Int): Int {
            return (bytes[offset].toInt() and 0xFF) or
                    ((bytes[offset + 1].toInt() and 0xFF) shl 8)
        }
    }
}
