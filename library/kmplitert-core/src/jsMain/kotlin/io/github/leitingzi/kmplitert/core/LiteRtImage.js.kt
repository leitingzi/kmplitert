@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.leitingzi.kmplitert.core


import kotlinx.browser.document
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLImageElement

/**
 * LiteRtImage implementation for Web (JS/WasmJs).
 * Wraps an HTMLCanvasElement for image operations.
 */
@OptIn(ExperimentalWasmJsInterop::class)
actual class LiteRtImage (val canvas: HTMLCanvasElement) {

    actual fun resize(width: Int, height: Int): LiteRtImage {
        val resizedCanvas = document.createElement("canvas") as HTMLCanvasElement
        resizedCanvas.width = width
        resizedCanvas.height = height
        val ctx = resizedCanvas.getContext("2d") as CanvasRenderingContext2D
        ctx.drawImage(canvas, 0.0, 0.0, width.toDouble(), height.toDouble())
        return LiteRtImage(resizedCanvas)
    }

    actual fun toFloatArray(mean: Float, std: Float): FloatArray {
        val width = canvas.width
        val height = canvas.height
        val ctx = canvas.getContext("2d") as CanvasRenderingContext2D
        val imageData = ctx.getImageData(0.0, 0.0, width.toDouble(), height.toDouble())
        val data = imageData.data // Uint8ClampedArray

        val floatArray = FloatArray(width * height * 3)
        for (i in 0 until width * height) {
            val r = data.asDynamic()[i * 4].unsafeCast<Int>().toFloat()
            val g = data.asDynamic()[i * 4 + 1].unsafeCast<Int>().toFloat()
            val b = data.asDynamic()[i * 4 + 2].unsafeCast<Int>().toFloat()
            floatArray[i * 3] = (r - mean) / std
            floatArray[i * 3 + 1] = (g - mean) / std
            floatArray[i * 3 + 2] = (b - mean) / std
        }
        return floatArray
    }

    actual fun toInt8Array(): ByteArray {
        val width = canvas.width
        val height = canvas.height
        val ctx = canvas.getContext("2d") as CanvasRenderingContext2D
        val imageData = ctx.getImageData(0.0, 0.0, width.toDouble(), height.toDouble())
        val data = imageData.data

        val byteArray = ByteArray(width * height * 3)
        for (i in 0 until width * height) {
            byteArray[i * 3] = data.asDynamic()[i * 4].unsafeCast<Int>().toByte()
            byteArray[i * 3 + 1] = data.asDynamic()[i * 4 + 1].unsafeCast<Int>().toByte()
            byteArray[i * 3 + 2] = data.asDynamic()[i * 4 + 2].unsafeCast<Int>().toByte()
        }
        return byteArray
    }

    actual fun toIntArray(): IntArray {
        val width = canvas.width
        val height = canvas.height
        val ctx = canvas.getContext("2d") as CanvasRenderingContext2D
        val imageData = ctx.getImageData(0.0, 0.0, width.toDouble(), height.toDouble())
        val data = imageData.data

        val intArray = IntArray(width * height * 3)
        for (i in 0 until width * height) {
            intArray[i * 3] = data.asDynamic()[i * 4].unsafeCast<Int>()
            intArray[i * 3 + 1] = data.asDynamic()[i * 4 + 1].unsafeCast<Int>()
            intArray[i * 3 + 2] = data.asDynamic()[i * 4 + 2].unsafeCast<Int>()
        }
        return intArray
    }

    actual fun toBooleanArray(): BooleanArray {
        val width = canvas.width
        val height = canvas.height
        val ctx = canvas.getContext("2d") as CanvasRenderingContext2D
        val imageData = ctx.getImageData(0.0, 0.0, width.toDouble(), height.toDouble())
        val data = imageData.data

        val booleanArray = BooleanArray(width * height * 3)
        for (i in 0 until width * height) {
            booleanArray[i * 3] = data.asDynamic()[i * 4].unsafeCast<Int>() > 127
            booleanArray[i * 3 + 1] = data.asDynamic()[i * 4 + 1].unsafeCast<Int>() > 127
            booleanArray[i * 3 + 2] = data.asDynamic()[i * 4 + 2].unsafeCast<Int>() > 127
        }
        return booleanArray
    }

    actual fun toLongArray(): LongArray {
        val width = canvas.width
        val height = canvas.height
        val ctx = canvas.getContext("2d") as CanvasRenderingContext2D
        val imageData = ctx.getImageData(0.0, 0.0, width.toDouble(), height.toDouble())
        val data = imageData.data

        val longArray = LongArray(width * height * 3)
        for (i in 0 until width * height) {
            longArray[i * 3] = data.asDynamic()[i * 4].unsafeCast<Int>().toLong()
            longArray[i * 3 + 1] = data.asDynamic()[i * 4 + 1].unsafeCast<Int>().toLong()
            longArray[i * 3 + 2] = data.asDynamic()[i * 4 + 2].unsafeCast<Int>().toLong()
        }
        return longArray
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

            for (i in 0 until width * height) {
                val base = i * 4
                val dataBase = i * 3

                pixels.asDynamic()[base] = data[dataBase].toInt() and 0xFF
                pixels.asDynamic()[base + 1] = data[dataBase + 1].toInt() and 0xFF
                pixels.asDynamic()[base + 2] = data[dataBase + 2].toInt() and 0xFF
                pixels.asDynamic()[base + 3] = 255
            }
            ctx.putImageData(imageData, 0.0, 0.0)
            return LiteRtImage(canvas)
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


