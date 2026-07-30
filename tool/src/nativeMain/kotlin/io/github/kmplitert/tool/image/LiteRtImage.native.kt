@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.kmplitert.tool.image

import io.github.kmplitert.core.TFBuffer
import io.github.kmplitert.tool.image.ImageFlip
import io.github.kmplitert.tool.image.ImageRotation
import io.github.kmplitert.tool.image.LiteRtImage

actual class LiteRtImage(
    val data: ByteArray,
    actual val width: Int,
    actual val height: Int,
    actual val channels: Int = 3
) {
    actual fun resize(width: Int, height: Int): LiteRtImage {
        val newData = ByteArray(width * height * channels)
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
                
                for (c in 0 until channels) {
                    val a = this.data[((y * this.width + x) * channels + c)].toInt() and 0xFF
                    val b = this.data[((y * this.width + xNext) * channels + c)].toInt() and 0xFF
                    val d = this.data[((yNext * this.width + x) * channels + c)].toInt() and 0xFF
                    val e = this.data[((yNext * this.width + xNext) * channels + c)].toInt() and 0xFF

                    // Y = A(1-w)(1-h) + B(w)(1-h) + D(h)(1-w) + E(wh)
                    val pixel = (a * (1 - xDiff) * (1 - yDiff) +
                            b * (xDiff) * (1 - yDiff) +
                            d * (yDiff) * (1 - xDiff) +
                            e * (yDiff * xDiff)).toInt()

                    newData[((i * width + j) * channels + c)] = pixel.coerceIn(0, 255).toByte()
                }
            }
        }
        return LiteRtImage(newData, width, height, channels)
    }

    actual fun crop(x: Int, y: Int, width: Int, height: Int): LiteRtImage {
        val newData = ByteArray(width * height * channels)
        for (i in 0 until height) {
            val srcY = y + i
            if (srcY < 0 || srcY >= this.height) continue
            for (j in 0 until width) {
                val srcX = x + j
                if (srcX < 0 || srcX >= this.width) continue
                for (c in 0 until channels) {
                    newData[(i * width + j) * channels + c] = this.data[(srcY * this.width + srcX) * channels + c]
                }
            }
        }
        return LiteRtImage(newData, width, height, channels)
    }

    actual fun centerCrop(width: Int, height: Int): LiteRtImage {
        val left = (this.width - width) / 2
        val top = (this.height - height) / 2
        return crop(left, top, width, height)
    }

    actual fun rotate(degrees: Float): LiteRtImage {
        val normalizedDegrees = ((degrees % 360 + 360) % 360).toInt()
        return when (normalizedDegrees) {
            90 -> rotate90()
            180 -> rotate180()
            270 -> rotate270()
            else -> this 
        }
    }

    private fun rotate90(): LiteRtImage {
        val newData = ByteArray(width * height * channels)
        for (y in 0 until height) {
            for (x in 0 until width) {
                for (c in 0 until channels) {
                    newData[(x * height + (height - 1 - y)) * channels + c] = data[(y * width + x) * channels + c]
                }
            }
        }
        return LiteRtImage(newData, height, width, channels)
    }

    private fun rotate180(): LiteRtImage {
        val newData = ByteArray(width * height * channels)
        for (y in 0 until height) {
            for (x in 0 until width) {
                for (c in 0 until channels) {
                    newData[((height - 1 - y) * width + (width - 1 - x)) * channels + c] = data[(y * width + x) * channels + c]
                }
            }
        }
        return LiteRtImage(newData, width, height, channels)
    }

    private fun rotate270(): LiteRtImage {
        val newData = ByteArray(width * height * channels)
        for (y in 0 until height) {
            for (x in 0 until width) {
                for (c in 0 until channels) {
                    newData[((width - 1 - x) * height + y) * channels + c] = data[(y * width + x) * channels + c]
                }
            }
        }
        return LiteRtImage(newData, height, width, channels)
    }

    actual fun flip(horizontal: Boolean, vertical: Boolean): LiteRtImage {
        val newData = ByteArray(width * height * channels)
        for (y in 0 until height) {
            val srcY = if (vertical) height - 1 - y else y
            for (x in 0 until width) {
                val srcX = if (horizontal) width - 1 - x else x
                for (c in 0 until channels) {
                    newData[(y * width + x) * channels + c] = data[(srcY * this.width + srcX) * channels + c]
                }
            }
        }
        return LiteRtImage(newData, width, height, channels)
    }

    actual fun toGrayscale(): LiteRtImage {
        if (channels == 1) return this
        val newData = ByteArray(width * height)
        for (i in 0 until width * height) {
            val r = data[i * channels].toInt() and 0xFF
            val g = data[i * channels + 1].toInt() and 0xFF
            val b = data[i * channels + 2].toInt() and 0xFF
            val gray = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
            newData[i] = gray.coerceIn(0, 255).toByte()
        }
        return LiteRtImage(newData, width, height, 1)
    }

    actual fun toRgb(): LiteRtImage {
        if (channels == 3) return this
        val newData = ByteArray(width * height * 3)
        for (i in 0 until width * height) {
            if (channels == 1) {
                val gray = data[i]
                newData[i * 3] = gray
                newData[i * 3 + 1] = gray
                newData[i * 3 + 2] = gray
            } else if (channels == 4) {
                newData[i * 3] = data[i * 4]
                newData[i * 3 + 1] = data[i * 4 + 1]
                newData[i * 3 + 2] = data[i * 4 + 2]
            }
        }
        return LiteRtImage(newData, width, height, 3)
    }

    actual fun toFloatArray(mean: Float, std: Float): FloatArray {
        val floatArray = FloatArray(width * height * channels)
        for (i in 0 until width * height * channels) {
            val value = data[i].toInt() and 0xFF
            floatArray[i] = (value - mean) / std
        }
        return floatArray
    }

    actual fun toInt8Array(): ByteArray {
        return data.copyOf()
    }

    actual fun toIntArray(): IntArray {
        val result = IntArray(width * height * channels)
        for (i in 0 until width * height * channels) {
            result[i] = data[i].toInt() and 0xFF
        }
        return result
    }

    actual fun toBooleanArray(): BooleanArray {
        val result = BooleanArray(width * height * channels)
        for (i in 0 until width * height * channels) {
            result[i] = (data[i].toInt() and 0xFF) > 127
        }
        return result
    }

    actual fun toLongArray(): LongArray {
        val result = LongArray(width * height * channels)
        for (i in 0 until width * height * channels) {
            result[i] = (data[i].toInt() and 0xFF).toLong()
        }
        return result
    }

    actual fun writeInt8Buffer(buffer: TFBuffer) {
        buffer.writeInt8(data)
    }

    actual fun writeFloatBuffer(buffer: TFBuffer, mean: Float, std: Float) {
        buffer.writeFloat(toFloatArray(mean, std))
    }

    actual companion object {
        actual fun fromBytes(bytes: ByteArray): LiteRtImage {
            if (bytes.size > 54 && bytes[0] == 'B'.code.toByte() && bytes[1] == 'M'.code.toByte()) {
                return decodeBmp(bytes)
            }
            throw UnsupportedOperationException("Only BMP format is supported in pure Kotlin native implementation for now.")
        }

        actual fun fromRawRgb(data: ByteArray, width: Int, height: Int): LiteRtImage {
            return LiteRtImage(data, width, height, 3)
        }

        actual fun fromVideoFrame(
            frame: Any,
            rotation: ImageRotation,
            flip: ImageFlip
        ): LiteRtImage {
            return fromVideoFrameNative(frame, rotation, flip)
        }

        private fun decodeBmp(bytes: ByteArray): LiteRtImage {
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

internal expect fun fromVideoFrameNative(
    frame: Any,
    rotation: ImageRotation,
    flip: ImageFlip
): LiteRtImage
