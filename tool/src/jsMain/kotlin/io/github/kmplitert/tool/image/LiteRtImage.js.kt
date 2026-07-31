@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.kmplitert.tool.image


import io.github.kmplitert.core.TFBuffer
import io.github.kmplitert.tool.image.ImageFlip
import io.github.kmplitert.tool.image.ImageRotation
import io.github.kmplitert.tool.image.LiteRtImage
import kotlinx.browser.document
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLImageElement

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
        val data = imageData.data

        val floatArray = FloatArray(width * height * c)
        for (i in 0 until width * height) {
            val base = i * 4
            val dst = i * c
            if (c >= 3) {
                floatArray[dst] = (data.asDynamic()[base].unsafeCast<Int>().toFloat() - mean) / std
                floatArray[dst + 1] = (data.asDynamic()[base + 1].unsafeCast<Int>().toFloat() - mean) / std
                floatArray[dst + 2] = (data.asDynamic()[base + 2].unsafeCast<Int>().toFloat() - mean) / std
                if (c == 4) {
                    floatArray[dst + 3] = (data.asDynamic()[base + 3].unsafeCast<Int>().toFloat() - mean) / std
                }
            } else if (c == 1) {
                floatArray[dst] = (data.asDynamic()[base].unsafeCast<Int>().toFloat() - mean) / std
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
        val data = imageData.data

        val byteArray = ByteArray(width * height * c)
        for (i in 0 until width * height) {
            val base = i * 4
            val dst = i * c
            if (c >= 3) {
                byteArray[dst] = data.asDynamic()[base].unsafeCast<Int>().toByte()
                byteArray[dst + 1] = data.asDynamic()[base + 1].unsafeCast<Int>().toByte()
                byteArray[dst + 2] = data.asDynamic()[base + 2].unsafeCast<Int>().toByte()
                if (c == 4) {
                    byteArray[dst + 3] = data.asDynamic()[base + 3].unsafeCast<Int>().toByte()
                }
            } else if (c == 1) {
                byteArray[dst] = data.asDynamic()[base].unsafeCast<Int>().toByte()
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
        val data = imageData.data

        val intArray = IntArray(width * height * c)
        for (i in 0 until width * height) {
            val base = i * 4
            val dst = i * c
            if (c >= 3) {
                intArray[dst] = data.asDynamic()[base].unsafeCast<Int>()
                intArray[dst + 1] = data.asDynamic()[base + 1].unsafeCast<Int>()
                intArray[dst + 2] = data.asDynamic()[base + 2].unsafeCast<Int>()
                if (c == 4) {
                    intArray[dst + 3] = data.asDynamic()[base + 3].unsafeCast<Int>()
                }
            } else if (c == 1) {
                intArray[dst] = data.asDynamic()[base].unsafeCast<Int>()
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
        val data = imageData.data

        val booleanArray = BooleanArray(width * height * c)
        for (i in 0 until width * height) {
            val base = i * 4
            val dst = i * c
            if (c >= 3) {
                booleanArray[dst] = data.asDynamic()[base].unsafeCast<Int>() > 127
                booleanArray[dst + 1] = data.asDynamic()[base + 1].unsafeCast<Int>() > 127
                booleanArray[dst + 2] = data.asDynamic()[base + 2].unsafeCast<Int>() > 127
                if (c == 4) {
                    booleanArray[dst + 3] = data.asDynamic()[base + 3].unsafeCast<Int>() > 127
                }
            } else if (c == 1) {
                booleanArray[dst] = data.asDynamic()[base].unsafeCast<Int>() > 127
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
        val data = imageData.data

        val longArray = LongArray(width * height * c)
        for (i in 0 until width * height) {
            val base = i * 4
            val dst = i * c
            if (c >= 3) {
                longArray[dst] = data.asDynamic()[base].unsafeCast<Int>().toLong()
                longArray[dst + 1] = data.asDynamic()[base + 1].unsafeCast<Int>().toLong()
                longArray[dst + 2] = data.asDynamic()[base + 2].unsafeCast<Int>().toLong()
                if (c == 4) {
                    longArray[dst + 3] = data.asDynamic()[base + 3].unsafeCast<Int>().toLong()
                }
            } else if (c == 1) {
                longArray[dst] = data.asDynamic()[base].unsafeCast<Int>().toLong()
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
            return LiteRtImage(canvas, 3)
        }

        actual fun fromVideoFrame(
            frame: Any,
            rotation: ImageRotation,
            flip: ImageFlip
        ): LiteRtImage {
            val canvas = document.createElement("canvas") as HTMLCanvasElement
            val ctx = canvas.getContext("2d") as CanvasRenderingContext2D
            
            when (frame) {
                is org.w3c.dom.HTMLVideoElement -> {
                    canvas.width = frame.videoWidth
                    canvas.height = frame.videoHeight
                    ctx.drawImage(frame, 0.0, 0.0)
                }
                is org.w3c.dom.HTMLImageElement -> {
                    canvas.width = frame.width
                    canvas.height = frame.height
                    ctx.drawImage(frame, 0.0, 0.0)
                }
                is org.w3c.dom.HTMLCanvasElement -> {
                    canvas.width = frame.width
                    canvas.height = frame.height
                    ctx.drawImage(frame, 0.0, 0.0)
                }
                else -> throw IllegalArgumentException("Unsupported frame type on Web: ${frame::class.js.name}")
            }
            
            var image = LiteRtImage(canvas)
            if (rotation != ImageRotation.ROTATION_0) image = image.rotate(rotation.degrees.toFloat())
            if (flip.horizontal || flip.vertical) image = image.flip(flip.horizontal, flip.vertical)
            return image
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
