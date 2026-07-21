@file:OptIn(ExperimentalForeignApi::class)

package io.github.kmplitert.tool

import kotlinx.cinterop.*
import platform.CoreVideo.*
import platform.CoreGraphics.*
import platform.UIKit.*

/**
 * Creates a [LiteRtImage] from an iOS [CVPixelBufferRef].
 *
 * This function handles common pixel formats like BGRA and YUV efficiently.
 */
fun LiteRtImage.Companion.fromIosPixelBuffer(pixelBuffer: CVPixelBufferRef): LiteRtImage {
    CVPixelBufferLockBaseAddress(pixelBuffer, kCVPixelBufferLock_ReadOnly)
    try {
        val width = CVPixelBufferGetWidth(pixelBuffer).toInt()
        val height = CVPixelBufferGetHeight(pixelBuffer).toInt()
        val format = CVPixelBufferGetPixelFormatType(pixelBuffer)

        return when (format) {
            kCVPixelFormatType_32BGRA -> {
                val baseAddress = CVPixelBufferGetBaseAddress(pixelBuffer)!!.reinterpret<ByteVar>()
                val bytesPerRow = CVPixelBufferGetBytesPerRow(pixelBuffer).toInt()
                val data = ByteArray(width * height * 3)
                
                for (y in 0 until height) {
                    val rowOffset = y * bytesPerRow
                    for (x in 0 until width) {
                        val b = baseAddress[rowOffset + x * 4]
                        val g = baseAddress[rowOffset + x * 4 + 1]
                        val r = baseAddress[rowOffset + x * 4 + 2]
                        // Skip Alpha (baseAddress[rowOffset + x * 4 + 3])
                        
                        val destOffset = (y * width + x) * 3
                        data[destOffset] = r
                        data[destOffset + 1] = g
                        data[destOffset + 2] = b
                    }
                }
                LiteRtImage(data, width, height, 3)
            }
            kCVPixelFormatType_420YpCbCr8BiPlanarVideoRange,
            kCVPixelFormatType_420YpCbCr8BiPlanarFullRange -> {
                // YUV 420 Bi-Planar (NV12/NV21)
                val yBaseAddress = CVPixelBufferGetBaseAddressOfPlane(pixelBuffer, 0L)!!.reinterpret<ByteVar>()
                val yBytesPerRow = CVPixelBufferGetBytesPerRowOfPlane(pixelBuffer, 0L).toInt()
                
                val uvBaseAddress = CVPixelBufferGetBaseAddressOfPlane(pixelBuffer, 1L)!!.reinterpret<ByteVar>()
                val uvBytesPerRow = CVPixelBufferGetBytesPerRowOfPlane(pixelBuffer, 1L).toInt()
                
                val data = ByteArray(width * height * 3)
                
                for (y in 0 until height) {
                    val yRowOffset = y * yBytesPerRow
                    val uvRowOffset = (y / 2) * uvBytesPerRow
                    for (x in 0 until width) {
                        val yVal = yBaseAddress[yRowOffset + x].toInt() and 0xFF
                        
                        // NV12: UVUV...
                        val uvIndex = (x / 2) * 2
                        val uVal = (uvBaseAddress[uvRowOffset + uvIndex].toInt() and 0xFF) - 128
                        val vVal = (uvBaseAddress[uvRowOffset + uvIndex + 1].toInt() and 0xFF) - 128
                        
                        val r = (yVal + 1.370705f * vVal).toInt().coerceIn(0, 255)
                        val g = (yVal - 0.337633f * uVal - 0.698001f * vVal).toInt().coerceIn(0, 255)
                        val b = (yVal + 1.732446f * uVal).toInt().coerceIn(0, 255)
                        
                        val destOffset = (y * width + x) * 3
                        data[destOffset] = r.toByte()
                        data[destOffset + 1] = g.toByte()
                        data[destOffset + 2] = b.toByte()
                    }
                }
                LiteRtImage(data, width, height, 3)
            }
            else -> throw UnsupportedOperationException("Unsupported PixelBuffer format: $format")
        }
    } finally {
        CVPixelBufferUnlockBaseAddress(pixelBuffer, kCVPixelBufferLock_ReadOnly)
    }
}
