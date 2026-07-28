@file:OptIn(ExperimentalForeignApi::class)
@file:Suppress("CANNOT_CHECK_FOR_ERASED_TYPE")

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
    @Suppress("CANNOT_CHECK_FOR_ERASED_TYPE")
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
                this.data = argbData
                this.width = width.convert()
                this.height = height.convert()
                this.rowBytes = (width * 4).convert()
            }

            if (pixelFormat == kCVPixelFormatType_420YpCbCr8BiPlanarVideoRange ||
                pixelFormat == kCVPixelFormatType_420YpCbCr8BiPlanarFullRange
            ) {
                val srcYPtr = CVPixelBufferGetBaseAddressOfPlane(pixelBuffer, 0u)
                val srcYRowBytes = CVPixelBufferGetBytesPerRowOfPlane(pixelBuffer, 0u)
                val srcUVPtr = CVPixelBufferGetBaseAddressOfPlane(pixelBuffer, 1u)
                val srcUVRowBytes = CVPixelBufferGetBytesPerRowOfPlane(pixelBuffer, 1u)

                val srcBufferY = alloc<vImage_Buffer>().apply {
                    this.data = srcYPtr
                    this.width = width.convert()
                    this.height = height.convert()
                    this.rowBytes = srcYRowBytes.convert()
                }
                val srcBufferUV = alloc<vImage_Buffer>().apply {
                    this.data = srcUVPtr
                    this.width = (width / 2).convert()
                    this.height = (height / 2).convert()
                    this.rowBytes = srcUVRowBytes.convert()
                }

                val infoYUV = alloc<vImage_YpCbCrToARGB>()
                val pixelRange = alloc<vImage_YpCbCrPixelRange>().apply {
                    Yp_bias = 16
                    CbCr_bias = 128
                    YpRangeMax = 235
                    CbCrRangeMax = 240
                    YpMax = 255
                    YpMin = 0
                    CbCrMax = 255
                    CbCrMin = 0
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
                    this.data = srcPtr
                    this.width = width.convert()
                    this.height = height.convert()
                    this.rowBytes = srcRowBytes.convert()
                }
                // BGRA to ARGB (vImage usually handles ARGB internally or we can just treat it as such if consistent)
                // Actually BGRA 8888 to ARGB 8888 if needed.
                vImageCopyBuffer(srcBuffer.ptr, argbBuffer.ptr, 4.convert(), kvImageNoFlags)
            } else {
                throw IllegalArgumentException("Unsupported pixel format: $pixelFormat")
            }

            val targetWidth = if (rotation == LiteRtRotation.ROTATION_90 || rotation == LiteRtRotation.ROTATION_270) height else width
            val targetHeight = if (rotation == LiteRtRotation.ROTATION_90 || rotation == LiteRtRotation.ROTATION_270) width else height
            
            val finalData = nativeHeap.allocArray<ByteVar>(targetWidth * targetHeight * 4)
            val finalBuffer = alloc<vImage_Buffer>().apply {
                this.data = finalData
                this.width = targetWidth.convert()
                this.height = targetHeight.convert()
                this.rowBytes = (targetWidth * 4).convert()
            }

            when (rotation) {
                LiteRtRotation.ROTATION_90 -> {
                    val backColor = nativeHeap.allocArray<UByteVar>(4)
                    vImageRotate90_ARGB8888(argbBuffer.ptr, finalBuffer.ptr, 0u, backColor, kvImageNoFlags)
                    nativeHeap.free(backColor)
                }
                LiteRtRotation.ROTATION_180 -> {
                    val backColor = nativeHeap.allocArray<UByteVar>(4)
                    val tempData = nativeHeap.allocArray<ByteVar>(width * height * 4)
                    val tempBuffer = alloc<vImage_Buffer>().apply {
                        this.data = tempData
                        this.width = width.convert()
                        this.height = height.convert()
                        this.rowBytes = (width * 4).convert()
                    }
                    vImageRotate90_ARGB8888(argbBuffer.ptr, tempBuffer.ptr, 0u, backColor, kvImageNoFlags)
                    vImageRotate90_ARGB8888(tempBuffer.ptr, finalBuffer.ptr, 0u, backColor, kvImageNoFlags)
                    nativeHeap.free(tempData)
                    nativeHeap.free(backColor)
                }
                LiteRtRotation.ROTATION_270 -> {
                    val backColor = nativeHeap.allocArray<UByteVar>(4)
                    // 270 = 90 * 3
                    val temp1Data = nativeHeap.allocArray<ByteVar>(targetWidth * targetHeight * 4)
                    val temp1Buffer = alloc<vImage_Buffer>().apply {
                        this.data = temp1Data
                        this.width = height.convert()
                        this.height = width.convert()
                        this.rowBytes = (height * 4).convert()
                    }
                    val temp2Data = nativeHeap.allocArray<ByteVar>(width * height * 4)
                    val temp2Buffer = alloc<vImage_Buffer>().apply {
                        this.data = temp2Data
                        this.width = width.convert()
                        this.height = height.convert()
                        this.rowBytes = (width * 4).convert()
                    }
                    vImageRotate90_ARGB8888(argbBuffer.ptr, temp1Buffer.ptr, 0u, backColor, kvImageNoFlags)
                    vImageRotate90_ARGB8888(temp1Buffer.ptr, temp2Buffer.ptr, 0u, backColor, kvImageNoFlags)
                    vImageRotate90_ARGB8888(temp2Buffer.ptr, finalBuffer.ptr, 0u, backColor, kvImageNoFlags)
                    nativeHeap.free(temp1Data)
                    nativeHeap.free(temp2Data)
                    nativeHeap.free(backColor)
                }
                else -> vImageCopyBuffer(argbBuffer.ptr, finalBuffer.ptr, 4.convert(), kvImageNoFlags)
            }
            
            if (flip.horizontal || flip.vertical) {
                val dataPtr = finalBuffer.data!!.reinterpret<UByteVar>()
                val rowBytes = finalBuffer.rowBytes.toInt()
                val tempPixel = nativeHeap.allocArray<UByteVar>(4)
                
                if (flip.vertical) {
                    for (y in 0 until targetHeight / 2) {
                        val topRowIdx = y * rowBytes
                        val bottomRowIdx = (targetHeight - 1 - y) * rowBytes
                        for (x in 0 until targetWidth) {
                            val topIdx = topRowIdx + x * 4
                            val bottomIdx = bottomRowIdx + x * 4
                            for (c in 0 until 4) {
                                tempPixel[c] = dataPtr[topIdx + c]
                                dataPtr[topIdx + c] = dataPtr[bottomIdx + c]
                                dataPtr[bottomIdx + c] = tempPixel[c]
                            }
                        }
                    }
                }
                
                if (flip.horizontal) {
                    for (y in 0 until targetHeight) {
                        val rowIdx = y * rowBytes
                        for (x in 0 until targetWidth / 2) {
                            val leftIdx = rowIdx + x * 4
                            val rightIdx = rowIdx + (targetWidth - 1 - x) * 4
                            for (c in 0 until 4) {
                                tempPixel[c] = dataPtr[leftIdx + c]
                                dataPtr[leftIdx + c] = dataPtr[rightIdx + c]
                                dataPtr[rightIdx + c] = tempPixel[c]
                            }
                        }
                    }
                }
                nativeHeap.free(tempPixel)
            }

            val resultData = ByteArray(targetWidth * targetHeight * 3)
            val finalDataPtr = finalData.reinterpret<UByteVar>()
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
