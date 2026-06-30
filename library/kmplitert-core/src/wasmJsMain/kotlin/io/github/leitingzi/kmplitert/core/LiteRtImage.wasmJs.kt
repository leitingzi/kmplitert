@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.leitingzi.kmplitert.core

import kotlinx.browser.document
import org.khronos.webgl.get
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLImageElement
import kotlin.js.ExperimentalWasmJsInterop

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
        val pixels = imageData.data // Uint8ClampedArray

        val floatArray = FloatArray(width * height * 3)
        for (i in 0 until width * height) {
            val base = i * 4

            val r = pixels[base].toFloat()
            val g = pixels[base + 1].toFloat()
            val b = pixels[base + 2].toFloat()

            val dst = i * 3
            floatArray[dst] = (r - mean) / std
            floatArray[dst + 1] = (g - mean) / std
            floatArray[dst + 2] = (b - mean) / std
        }
        return floatArray
    }

    actual companion object {
        /**
         * Note: Synchronous decoding of image bytes is not supported on Web.
         * This method will throw an exception if called.
         * Consider using a secondary constructor or factory method that takes a loaded HTMLImageElement or Canvas.
         */
        actual fun fromBytes(bytes: ByteArray): LiteRtImage {
            throw UnsupportedOperationException(
                "LiteRtImage.fromBytes is not supported on Web due to synchronous decoding limitations. " +
                        "Please use LiteRtImage.fromImageElement or similar async methods."
            )
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
    }
}
