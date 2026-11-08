@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.kmplitert.dataprocess.image

import kotlinx.browser.document
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.khronos.webgl.set
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLImageElement
import org.w3c.dom.HTMLVideoElement

internal actual fun decodeVideoFrame(frame: Any): LiteRtImage {
    val canvas = document.createElement("canvas") as HTMLCanvasElement
    val context = canvas.getContext("2d") as CanvasRenderingContext2D
    when (frame) {
        is HTMLVideoElement -> {
            canvas.width = frame.videoWidth
            canvas.height = frame.videoHeight
            require(canvas.width > 0 && canvas.height > 0) { "Video frame is not ready" }
            context.drawImage(frame, 0.0, 0.0)
        }
        is HTMLImageElement -> {
            canvas.width = frame.naturalWidth
            canvas.height = frame.naturalHeight
            require(canvas.width > 0 && canvas.height > 0) { "Image is not loaded" }
            context.drawImage(frame, 0.0, 0.0)
        }
        is HTMLCanvasElement -> return frame.asLiteRtImage()
        else -> throw IllegalArgumentException("Unsupported frame type on Web")
    }
    return canvas.asLiteRtImage()
}

fun LiteRtImage.Companion.fromImageElement(image: HTMLImageElement): LiteRtImage = fromVideoFrame(image)
fun LiteRtImage.Companion.fromCanvas(canvas: HTMLCanvasElement): LiteRtImage = canvas.asLiteRtImage()
fun HTMLImageElement.asLiteRtImage(): LiteRtImage = LiteRtImage.fromImageElement(this)

fun HTMLCanvasElement.asLiteRtImage(): LiteRtImage {
    val size = pixelCount(width, height, 4)
    val context = getContext("2d") as? CanvasRenderingContext2D
        ?: throw IllegalArgumentException("Canvas does not have a 2D context")
    val image = context.getImageData(0.0, 0.0, width.toDouble(), height.toDouble())
    val bytes = Uint8Array(image.data.buffer, image.data.byteOffset, image.data.length)
    return LiteRtImage.fromPixels(ByteArray(size) { bytes[it] }, width, height, 4)
}

fun LiteRtImage.asCanvas(): HTMLCanvasElement {
    val canvas = document.createElement("canvas") as HTMLCanvasElement
    canvas.width = width
    canvas.height = height
    val context = canvas.getContext("2d") as CanvasRenderingContext2D
    val image = context.createImageData(width.toDouble(), height.toDouble())
    val dest = Uint8Array(image.data.buffer, image.data.byteOffset, image.data.length)
    val source = data
    for (i in 0 until width * height) {
        val offset = i * channels
        dest[i * 4] = source[offset]
        dest[i * 4 + 1] = source[offset + if (channels == 1) 0 else 1]
        dest[i * 4 + 2] = source[offset + if (channels == 1) 0 else 2]
        dest[i * 4 + 3] = if (channels == 4) source[offset + 3] else (-1).toByte()
    }
    context.putImageData(image, 0.0, 0.0)
    return canvas
}
