@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
@file:Suppress("CANNOT_CHECK_FOR_ERASED_TYPE")

package io.github.kmplitert.dataprocess.image

import kotlinx.cinterop.*
import platform.CoreVideo.*

internal actual fun decodeVideoFrame(frame: Any): LiteRtImage {
    val pointer = frame as? CPointer<out CPointed>
    return if (pointer != null) {
        LiteRtImage.fromIosPixelBuffer(pointer.reinterpret())
    } else {
        fromAppleImage(frame)
    }
}

internal expect fun fromAppleImage(frame: Any): LiteRtImage

fun LiteRtImage.Companion.fromIosPixelBuffer(
    pixelBuffer: CVPixelBufferRef,
    rotation: ImageRotation = ImageRotation.ROTATION_0,
    flip: ImageFlip = ImageFlip(),
): LiteRtImage {
    require(CVPixelBufferLockBaseAddress(pixelBuffer, kCVPixelBufferLock_ReadOnly) == 0) {
        "Unable to lock pixel buffer"
    }
    try {
        val width = CVPixelBufferGetWidth(pixelBuffer).toInt()
        val height = CVPixelBufferGetHeight(pixelBuffer).toInt()
        val pixels = ByteArray(pixelCount(width, height, 3))
        val format = CVPixelBufferGetPixelFormatType(pixelBuffer)
        when (format) {
            kCVPixelFormatType_32BGRA -> {
                val base = requireNotNull(CVPixelBufferGetBaseAddress(pixelBuffer)).reinterpret<UByteVar>()
                val stride = CVPixelBufferGetBytesPerRow(pixelBuffer).toInt()
                for (y in 0 until height) for (x in 0 until width) {
                    val src = y * stride + x * 4
                    val dst = (y * width + x) * 3
                    pixels[dst] = base[src + 2].toByte()
                    pixels[dst + 1] = base[src + 1].toByte()
                    pixels[dst + 2] = base[src].toByte()
                }
            }
            kCVPixelFormatType_420YpCbCr8BiPlanarVideoRange,
            kCVPixelFormatType_420YpCbCr8BiPlanarFullRange -> {
                val yBase = requireNotNull(CVPixelBufferGetBaseAddressOfPlane(pixelBuffer, 0u)).reinterpret<UByteVar>()
                val uvBase = requireNotNull(CVPixelBufferGetBaseAddressOfPlane(pixelBuffer, 1u)).reinterpret<UByteVar>()
                val yStride = CVPixelBufferGetBytesPerRowOfPlane(pixelBuffer, 0u).toInt()
                val uvStride = CVPixelBufferGetBytesPerRowOfPlane(pixelBuffer, 1u).toInt()
                for (y in 0 until height) for (x in 0 until width) {
                    val uv = (y / 2) * uvStride + (x / 2) * 2
                    val rgb = yuvToRgb(yBase[y * yStride + x].toInt(), uvBase[uv].toInt(), uvBase[uv + 1].toInt(),
                        format == kCVPixelFormatType_420YpCbCr8BiPlanarFullRange)
                    val dst = (y * width + x) * 3
                    pixels[dst] = (rgb shr 16).toByte()
                    pixels[dst + 1] = (rgb shr 8).toByte()
                    pixels[dst + 2] = rgb.toByte()
                }
            }
            else -> throw IllegalArgumentException("Unsupported pixel format: $format")
        }
        return LiteRtImage.fromPixels(pixels, width, height, 3).transformFrame(rotation, flip)
    } finally {
        CVPixelBufferUnlockBaseAddress(pixelBuffer, kCVPixelBufferLock_ReadOnly)
    }
}
