@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.kmplitert.tool

import io.github.kmplitert.core.TFBuffer
import kotlinx.browser.document
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.Uint8ClampedArray
import org.khronos.webgl.get
import org.khronos.webgl.set
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLImageElement
import kotlin.js.unsafeCast

/**
 * LiteRtImage implementation for Web (JS/WasmJs).
 * Wraps an HTMLCanvasElement for image operations.
 */
@OptIn(ExperimentalWasmJsInterop::class)
actual class LiteRtImage (val canvas: HTMLCanvasElement, private val _channels: Int = 4) {
    actual val width: Int get() = canvas.width
    actual val height: Int get() = canvas.height
    actual val channels: Int get() = _channels

    actual fun resize(width: Int, height: Int): LiteRtImage {
        val resizedCanvas = document.createElement("canvas") as HTMLCanvasElement
        resizedCanvas.width = width
        resizedCanvas.height = height
        val ctx = resizedCanvas.getContext("2d") as CanvasRenderingContext2D
        ctx.drawImage(canvas, 0.0, 0.0, width.toDouble(), height.toDouble())
        return LiteRtImage(resizedCanvas, _channels)
    }

    actual fun crop(x: Int, y: Int, width: Int, height: Int): LiteRtImage {
        val croppedCanvas = document.createElement("canvas") as HTMLCanvasElement
        croppedCanvas.width = width
        croppedCanvas.height = height
        val ctx = croppedCanvas.getContext("2d") as CanvasRenderingContext2D
        ctx.drawImage(canvas, x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble(), 0.0, 0.0, width.toDouble(), height.toDouble())
        return LiteRtImage(croppedCanvas, _channels)
    }

    actual fun centerCrop(width: Int, height: Int): LiteRtImage {
        val left = (this.width - width) / 2
        val top = (this.height - height) / 2
        return crop(left.coerceAtLeast(0), top.coerceAtLeast(0), width, height)
    }

    actual fun rotate(degrees: Float): LiteRtImage {
        val rads = degrees * kotlin.math.PI / 180.0
        val sin = kotlin.math.abs(kotlin.math.sin(rads))
        val cos = kotlin.math.abs(kotlin.math.cos(rads))
        val w = width
        val h = height
        val newWidth = kotlin.math.floor(w * cos + h * sin).toInt()
        val newHeight = kotlin.math.floor(h * cos + w * sin).toInt()

        val rotatedCanvas = document.createElement("canvas") as HTMLCanvasElement
        rotatedCanvas.width = newWidth
        rotatedCanvas.height = newHeight
        val ctx = rotatedCanvas.getContext("2d") as CanvasRenderingContext2D
        ctx.translate(newWidth / 2.0, newHeight / 2.0)
        ctx.rotate(rads)
        ctx.drawImage(canvas, -w / 2.0, -h / 2.0)
        return LiteRtImage(rotatedCanvas, _channels)
    }

    actual fun flip(horizontal: Boolean, vertical: Boolean): LiteRtImage {
        val flippedCanvas = document.createElement("canvas") as HTMLCanvasElement
        flippedCanvas.width = width
        flippedCanvas.height = height
        val ctx = flippedCanvas.getContext("2d") as CanvasRenderingContext2D
        ctx.save()
        val scaleX = if (horizontal) -1.0 else 1.0
        val scaleY = if (vertical) -1.0 else 1.0
        ctx.scale(scaleX, scaleY)
        val drawX = if (horizontal) -width.toDouble() else 0.0
        val drawY = if (vertical) -height.toDouble() else 0.0
        ctx.drawImage(canvas, drawX, drawY)
        ctx.restore()
        return LiteRtImage(flippedCanvas, _channels)
    }

    actual fun toGrayscale(): LiteRtImage {
        val grayCanvas = document.createElement("canvas") as HTMLCanvasElement
        grayCanvas.width = width
        grayCanvas.height = height
        val ctx = grayCanvas.getContext("2d") as CanvasRenderingContext2D
        // filter might not be supported in all environments, but it's the standard way
        ctx.filter = "grayscale(100%)"
        ctx.drawImage(canvas, 0.0, 0.0)
        return LiteRtImage(grayCanvas, 1)
    }

    actual fun toRgb(): LiteRtImage {
        if (_channels == 3) return this
        return LiteRtImage(canvas, 3)
    }

    actual fun toFloatArray(mean: Float, std: Float): FloatArray {
        val width = canvas.width
        val height = canvas.height
        val c = _channels
        val ctx = canvas.getContext("2d") as CanvasRenderingContext2D
        val imageData = ctx.getImageData(0.0, 0.0, width.toDouble(), height.toDouble())
        val pixels = imageData.data // Uint8ClampedArray

        val floatArray = FloatArray(width * height * c)
        for (i in 0 until (width * height)) {
            val base = i * 4
            val dst = i * c

            if (c >= 3) {
                floatArray[dst] = ((pixels[base].toInt() and 0xFF).toFloat() - mean) / std
                floatArray[dst + 1] = ((pixels[base + 1].toInt() and 0xFF).toFloat() - mean) / std
                floatArray[dst + 2] = ((pixels[base + 2].toInt() and 0xFF).toFloat() - mean) / std
                if (c == 4) {
                    floatArray[dst + 3] = ((pixels[base + 3].toInt() and 0xFF).toFloat() - mean) / std
                }
            } else if (c == 1) {
                floatArray[dst] = ((pixels[base].toInt() and 0xFF).toFloat() - mean) / std
            }
        }
        return floatArray
    }

    actual fun toInt8Array(): ByteArray {
        val width = canvas.width
        val height = canvas.height
        val c = _channels
        val ctx = canvas.getContext("2d") as CanvasRenderingContext2D
        val imageData = ctx.getImageData(0.0, 0.0, width.toDouble(), height.toDouble())
        val pixels = imageData.data

        val byteArray = ByteArray(width * height * c)
        for (i in 0 until (width * height)) {
            val base = i * 4
            val dst = i * c
            if (c >= 3) {
                byteArray[dst] = (pixels[base].toInt() and 0xFF).toByte()
                byteArray[dst + 1] = (pixels[base + 1].toInt() and 0xFF).toByte()
                byteArray[dst + 2] = (pixels[base + 2].toInt() and 0xFF).toByte()
                if (c == 4) {
                    byteArray[dst + 3] = (pixels[base + 3].toInt() and 0xFF).toByte()
                }
            } else if (c == 1) {
                byteArray[dst] = (pixels[base].toInt() and 0xFF).toByte()
            }
        }
        return byteArray
    }

    actual fun toIntArray(): IntArray {
        val width = canvas.width
        val height = canvas.height
        val c = _channels
        val ctx = canvas.getContext("2d") as CanvasRenderingContext2D
        val imageData = ctx.getImageData(0.0, 0.0, width.toDouble(), height.toDouble())
        val pixels = imageData.data

        val intArray = IntArray(width * height * c)
        for (i in 0 until (width * height)) {
            val base = i * 4
            val dst = i * c
            if (c >= 3) {
                intArray[dst] = pixels[base].toInt() and 0xFF
                intArray[dst + 1] = pixels[base + 1].toInt() and 0xFF
                intArray[dst + 2] = pixels[base + 2].toInt() and 0xFF
                if (c == 4) {
                    intArray[dst + 3] = pixels[base + 3].toInt() and 0xFF
                }
            } else if (c == 1) {
                intArray[dst] = pixels[base].toInt() and 0xFF
            }
        }
        return intArray
    }

    actual fun toBooleanArray(): BooleanArray {
        val width = canvas.width
        val height = canvas.height
        val c = _channels
        val ctx = canvas.getContext("2d") as CanvasRenderingContext2D
        val imageData = ctx.getImageData(0.0, 0.0, width.toDouble(), height.toDouble())
        val pixels = imageData.data

        val booleanArray = BooleanArray(width * height * c)
        for (i in 0 until (width * height)) {
            val base = i * 4
            val dst = i * c
            if (c >= 3) {
                booleanArray[dst] = (pixels[base].toInt() and 0xFF) > 127
                booleanArray[dst + 1] = (pixels[base + 1].toInt() and 0xFF) > 127
                booleanArray[dst + 2] = (pixels[base + 2].toInt() and 0xFF) > 127
                if (c == 4) {
                    booleanArray[dst + 3] = (pixels[base + 3].toInt() and 0xFF) > 127
                }
            } else if (c == 1) {
                booleanArray[dst] = (pixels[base].toInt() and 0xFF) > 127
            }
        }
        return booleanArray
    }

    actual fun toLongArray(): LongArray {
        val width = canvas.width
        val height = canvas.height
        val c = _channels
        val ctx = canvas.getContext("2d") as CanvasRenderingContext2D
        val imageData = ctx.getImageData(0.0, 0.0, width.toDouble(), height.toDouble())
        val pixels = imageData.data

        val longArray = LongArray(width * height * c)
        for (i in 0 until (width * height)) {
            val base = i * 4
            val dst = i * c
            if (c >= 3) {
                longArray[dst] = (pixels[base].toInt() and 0xFF).toLong()
                longArray[dst + 1] = (pixels[base + 1].toInt() and 0xFF).toLong()
                longArray[dst + 2] = (pixels[base + 2].toInt() and 0xFF).toLong()
                if (c == 4) {
                    longArray[dst + 3] = (pixels[base + 3].toInt() and 0xFF).toLong()
                }
            } else if (c == 1) {
                longArray[dst] = (pixels[base].toInt() and 0xFF).toLong()
            }
        }
        return longArray
    }

    actual fun writeInt8Buffer(buffer: TFBuffer) {
        buffer.writeInt8(toInt8Array())
    }

    actual fun writeFloatBuffer(buffer: TFBuffer, mean: Float, std: Float) {
        buffer.writeFloat(toFloatArray(mean, std))
    }

    actual companion object {
        /**
         * Note: Synchronous decoding of image bytes is not supported on Web.
         * This method will throw an exception if called.
         * Consider using a secondary constructor or factory method that takes a loaded HTMLImageElement or Canvas.
         */
        actual fun fromBytes(bytes: ByteArray): LiteRtImage {
            if (bytes.size > 54 && bytes[0] == 'B'.code.toByte() && bytes[1] == 'M'.code.toByte()) {
                return decodeBmp(bytes)
            }
            throw UnsupportedOperationException(
                "LiteRtImage.fromBytes currently only supports BMP synchronously on Web. " +
                        "For PNG/JPEG, please use LiteRtImage.fromImageElement (async)."
            )
        }

        actual fun fromRawRgb(data: ByteArray, width: Int, height: Int): LiteRtImage {
            val canvas = document.createElement("canvas") as HTMLCanvasElement
            canvas.width = width
            canvas.height = height
            val ctx = canvas.getContext("2d") as CanvasRenderingContext2D
            val imageData = ctx.createImageData(width.toDouble(), height.toDouble())
            val pixels = imageData.data
            
            // Use a Uint8Array view to avoid clamping issues with signed Bytes in WasmJs interop.
            // Uint8Array will wrap around -1 to 255, whereas Uint8ClampedArray would clamp it to 0.
            val uint8View = Uint8Array(pixels.buffer, pixels.byteOffset, pixels.length)
            
            for (i in 0 until (width * height)) {
                val base = i * 4
                val dataBase = i * 3

                uint8View[base] = data[dataBase]
                uint8View[base + 1] = data[dataBase + 1]
                uint8View[base + 2] = data[dataBase + 2]
                uint8View[base + 3] = 255.toByte()
            }
            ctx.putImageData(imageData, 0.0, 0.0)
            return LiteRtImage(canvas, 3)
        }

        fun fromImageElement(image: HTMLImageElement): LiteRtImage {
            val canvas = document.createElement("canvas") as HTMLCanvasElement
            canvas.width = image.width
            canvas.height = image.height
            val ctx = canvas.getContext("2d") as CanvasRenderingContext2D
            ctx.drawImage(image, 0.0, 0.0)
            return LiteRtImage(canvas)
        }

        fun fromCanvas(canvas: HTMLCanvasElement): LiteRtImage {
            return LiteRtImage(canvas)
        }

        private fun decodeBmp(bytes: ByteArray): LiteRtImage {
            val width = readInt32(bytes, 18)
            val height = readInt32(bytes, 22)
            val offset = readInt32(bytes, 10)
            val bitsPerPixel = readInt16(bytes, 28)

            if (bitsPerPixel != 24) throw UnsupportedOperationException("Only 24-bit BMP is supported.")

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
            return fromRawRgb(data, width, height)
        }

        private fun readInt32(bytes: ByteArray, offset: Int): Int =
            (bytes[offset].toInt() and 0xFF) or ((bytes[offset+1].toInt() and 0xFF) shl 8) or
                    ((bytes[offset+2].toInt() and 0xFF) shl 16) or ((bytes[offset+3].toInt() and 0xFF) shl 24)

        private fun readInt16(bytes: ByteArray, offset: Int): Int =
            (bytes[offset].toInt() and 0xFF) or ((bytes[offset+1].toInt() and 0xFF) shl 8)
    }
}

fun LiteRtImage.asCanvas(): HTMLCanvasElement {
    return this.canvas
}

fun HTMLCanvasElement.asLiteRtImage(): LiteRtImage {
    return LiteRtImage(this)
}

fun HTMLImageElement.asLiteRtImage(): LiteRtImage {
    return LiteRtImage.fromImageElement(this)
}
