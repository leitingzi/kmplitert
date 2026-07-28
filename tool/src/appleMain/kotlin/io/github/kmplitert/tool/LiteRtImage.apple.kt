@file:OptIn(ExperimentalForeignApi::class)

package io.github.kmplitert.tool

import kotlinx.cinterop.*
import platform.CoreVideo.*
import platform.CoreGraphics.*
import platform.Accelerate.*

internal actual fun fromVideoFrameNative(
    frame: Any,
    rotation: LiteRtRotation,
    flip: LiteRtFlip
): LiteRtImage {
    if (frame is CVPixelBufferRef) {
        return LiteRtImage.Companion.fromIosPixelBuffer(frame, rotation, flip)
    }
    return fromPlatformImage(frame, rotation, flip)
}

internal expect fun fromPlatformImage(
    frame: Any,
    rotation: LiteRtRotation,
    flip: LiteRtFlip
): LiteRtImage

/**
 * Creates a [LiteRtImage] from an Apple [CVPixelBufferRef] with optional transformation.
 *
 * This function handles common pixel formats like BGRA and YUV efficiently using Accelerate framework.
 */
fun LiteRtImage.Companion.fromIosPixelBuffer(
    pixelBuffer: CVPixelBufferRef,
    rotation: LiteRtRotation = LiteRtRotation.ROTATION_0,
    flip: LiteRtFlip = LiteRtFlip()
): LiteRtImage {
    val pixelFormat = CVPixelBufferGetPixelFormatType(pixelBuffer)
    CVPixelBufferLockBaseAddress(pixelBuffer, kCVPixelBufferLock_ReadOnly)
    try {
        val width = CVPixelBufferGetWidth(pixelBuffer).toInt()
        val height = CVPixelBufferGetHeight(pixelBuffer).toInt()

        memScoped {
            val argbData = nativeHeap.allocArray<ByteVar>(width * height * 4)
            val argbBuffer = alloc<vImage_Buffer>().apply {
                data = argbData
                this.width = width.toULong()
                this.height = height.toULong()
                rowBytes = (width * 4).toULong()
            }

            if (pixelFormat == kCVPixelFormatType_420YpCbCr8BiPlanarVideoRange ||
                pixelFormat == kCVPixelFormatType_420YpCbCr8BiPlanarFullRange
            ) {
                val srcYPtr = CVPixelBufferGetBaseAddressOfPlane(pixelBuffer, 0u)
                val srcYRowBytes = CVPixelBufferGetBytesPerRowOfPlane(pixelBuffer, 0u)
                val srcUVPtr = CVPixelBufferGetBaseAddressOfPlane(pixelBuffer, 1u)
                val srcUVRowBytes = CVPixelBufferGetBytesPerRowOfPlane(pixelBuffer, 1u)

                val srcBufferY = alloc<vImage_Buffer>().apply {
                    data = srcYPtr
                    this.width = width.toULong()
                    this.height = height.toULong()
                    rowBytes = srcYRowBytes
                }
                val srcBufferUV = alloc<vImage_Buffer>().apply {
                    data = srcUVPtr
                    this.width = (width / 2).toULong()
                    this.height = (height / 2).toULong()
                    rowBytes = srcUVRowBytes
                }

                val infoYUV = alloc<vImage_YpCbCrToARGB>()
                val pixelRange = alloc<vImage_YpCbCrPixelRange>().apply {
                    Yp_bias = 16
                    CbCr_bias = 128
                    YpScale = 219
                    CbCrScale = 224
                }

                vImageConvert_YpCbCrToARGB_GenerateConversion(
                    kvImage_YpCbCrToARGBMatrix_ITU_R_601_4,
                    pixelRange.ptr,
                    infoYUV.ptr,
                    kvImage420Yp8_CbCr8,
                    kvImageARGB8888,
                    kvImageNoFlags
                )

                vImageConvert_420Yp8_CbCr8ToARGB8888(
                    srcBufferY.ptr,
                    srcBufferUV.ptr,
                    argbBuffer.ptr,
                    infoYUV.ptr,
                    null,
                    255u,
                    kvImageNoFlags
                )
            } else if (pixelFormat == kCVPixelFormatType_32BGRA) {
                val srcPtr = CVPixelBufferGetBaseAddress(pixelBuffer)
                val srcRowBytes = CVPixelBufferGetBytesPerRow(pixelBuffer)
                val srcBuffer = alloc<vImage_Buffer>().apply {
                    data = srcPtr
                    this.width = width.toULong()
                    this.height = height.toULong()
                    rowBytes = srcRowBytes
                }
                // BGRA to ARGB (vImage usually handles ARGB internally or we can just treat it as such if consistent)
                // Actually BGRA 8888 to ARGB 8888 if needed.
                vImageCopyBuffer(srcBuffer.ptr, argbBuffer.ptr, 4uL, kvImageNoFlags)
            } else {
                throw IllegalArgumentException("Unsupported pixel format: $pixelFormat")
            }

            val targetWidth = if (rotation == LiteRtRotation.ROTATION_90 || rotation == LiteRtRotation.ROTATION_270) height else width
            val targetHeight = if (rotation == LiteRtRotation.ROTATION_90 || rotation == LiteRtRotation.ROTATION_270) width else height
            
            val finalData = nativeHeap.allocArray<ByteVar>(targetWidth * targetHeight * 4)
            val finalBuffer = alloc<vImage_Buffer>().apply {
                data = finalData
                width = targetWidth.toULong()
                height = targetHeight.toULong()
                rowBytes = (targetWidth * 4).toULong()
            }

            when (rotation) {
                LiteRtRotation.ROTATION_90 -> vImageRotate90_ARGB8888(argbBuffer.ptr, finalBuffer.ptr, 0u, null, kvImageNoFlags)
                LiteRtRotation.ROTATION_180 -> vImageRotate180_ARGB8888(argbBuffer.ptr, finalBuffer.ptr, kvImageNoFlags)
                LiteRtRotation.ROTATION_270 -> vImageRotate270_ARGB8888(argbBuffer.ptr, finalBuffer.ptr, 0u, null, kvImageNoFlags)
                else -> vImageCopyBuffer(argbBuffer.ptr, finalBuffer.ptr, 4.toULong(), kvImageNoFlags)
            }
            
            if (flip.horizontal || flip.vertical) {
                if (flip.vertical) vImageVerticalFlip_ARGB8888(finalBuffer.ptr, finalBuffer.ptr, kvImageNoFlags)
                if (flip.horizontal) vImageHorizontalFlip_ARGB8888(finalBuffer.ptr, finalBuffer.ptr, kvImageNoFlags)
            }

            val resultData = ByteArray(targetWidth * targetHeight * 3)
            val finalDataPtr = finalData.reinterpret<uint8_tVar>()
            for (i in 0 until targetWidth * targetHeight) {
                resultData[i * 3] = finalDataPtr[i * 4 + 2].toByte()     // R
                resultData[i * 3 + 1] = finalDataPtr[i * 4 + 1].toByte() // G
                resultData[i * 3 + 2] = finalDataPtr[i * 4].toByte()     // B
            }
            
            nativeHeap.free(argbData)
            nativeHeap.free(finalData)
            
            return LiteRtImage(resultData, targetWidth, targetHeight, 3)
        }
    } finally {
        CVPixelBufferUnlockBaseAddress(pixelBuffer, kCVPixelBufferLock_ReadOnly)
    }
}
