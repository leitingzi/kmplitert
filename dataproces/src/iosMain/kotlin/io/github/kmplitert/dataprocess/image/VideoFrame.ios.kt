@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package io.github.kmplitert.dataprocess.image

import kotlinx.cinterop.*
import platform.CoreGraphics.*
import platform.UIKit.*

internal actual fun fromAppleImage(frame: Any): LiteRtImage {
    require(frame is UIImage) { "Unsupported frame type on iOS" }
    val image = requireNotNull(frame.CGImage) { "UIImage has no CGImage" }
    val width = CGImageGetWidth(image).toInt()
    val height = CGImageGetHeight(image).toInt()
    val rgba = ByteArray(pixelCount(width, height, 4))
    val rgb = ByteArray(pixelCount(width, height, 3))
    rgba.usePinned { pinned ->
        val colorSpace = CGColorSpaceCreateDeviceRGB()
        val context = CGBitmapContextCreate(
            pinned.addressOf(0), width.convert(), height.convert(), 8u, (width * 4).convert(),
            colorSpace, CGImageAlphaInfo.kCGImageAlphaNoneSkipLast.value or kCGBitmapByteOrder32Big.toUInt(),
        )
        if (context == null) {
            CGColorSpaceRelease(colorSpace)
            throw IllegalArgumentException("Unable to create image context")
        }
        try {
            UIGraphicsPushContext(context)
            try {
                frame.drawInRect(CGRectMake(0.0, 0.0, width.toDouble(), height.toDouble()))
            } finally {
                UIGraphicsPopContext()
            }
        } finally {
            CGContextRelease(context)
            CGColorSpaceRelease(colorSpace)
        }
    }
    for (i in 0 until width * height) {
        rgba.copyInto(rgb, i * 3, i * 4, i * 4 + 3)
    }
    return LiteRtImage.fromPixels(rgb, width, height, 3)
}
