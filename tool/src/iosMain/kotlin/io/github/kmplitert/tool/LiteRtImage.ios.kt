@file:OptIn(ExperimentalForeignApi::class)

package io.github.kmplitert.tool

import kotlinx.cinterop.*
import platform.CoreGraphics.*
import platform.UIKit.*

internal actual fun fromPlatformImage(
    frame: Any,
    rotation: LiteRtRotation,
    flip: LiteRtFlip
): LiteRtImage {
    if (frame is UIImage) {
        val bytes = frame.toRgbBytes() ?: throw IllegalArgumentException("Failed to get RGB bytes from UIImage")
        var image = LiteRtImage(bytes, frame.size.useContents { width.toInt() }, frame.size.useContents { height.toInt() }, 3)
        if (rotation != LiteRtRotation.ROTATION_0) image = image.rotate(rotation.degrees.toFloat())
        if (flip.horizontal || flip.vertical) image = image.flip(flip.horizontal, flip.vertical)
        return image
    }
    throw IllegalArgumentException("Unsupported frame type on iOS: ${frame::class}")
}

private fun UIImage.toRgbBytes(): ByteArray? {
    val imageRef = this.CGImage ?: return null
    val width = CGImageGetWidth(imageRef)
    val height = CGImageGetHeight(imageRef)
    val colorSpace = CGColorSpaceCreateDeviceRGB()
    val bytesPerPixel = 4uL
    val bytesPerRow = bytesPerPixel * width
    val bitsPerComponent = 8uL
    val bitmapInfo = CGImageAlphaInfo.kCGImageAlphaNoneSkipLast.value or kCGBitmapByteOrder32Big.value
    
    val dataSize = (width * height * bytesPerPixel).toLong()
    val data = nativeHeap.allocArray<uint8_tVar>(dataSize)
    val context = CGBitmapContextCreate(data, width, height, bitsPerComponent, bytesPerRow, colorSpace, bitmapInfo)
    
    if (context == null) {
        nativeHeap.free(data)
        return null
    }
    
    UIGraphicsPushContext(context)
    this.drawInRect(CGRectMake(0.0, 0.0, width.toDouble(), height.toDouble()))
    UIGraphicsPopContext()
    
    val result = ByteArray((width * height * 3).toInt())
    for (i in 0 until (width * height).toInt()) {
        result[i * 3] = data[i * 4 + 1].toByte()     // R
        result[i * 3 + 1] = data[i * 4 + 2].toByte() // G
        result[i * 3 + 2] = data[i * 4 + 3].toByte() // B
    }
    
    nativeHeap.free(data)
    CGContextRelease(context)
    return result
}
