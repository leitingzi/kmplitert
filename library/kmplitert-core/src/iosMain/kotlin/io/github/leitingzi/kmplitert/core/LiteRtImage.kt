package io.github.leitingzi.kmplitert.core

import platform.UIKit.*
import platform.CoreGraphics.*
import platform.Foundation.*
import kotlinx.cinterop.*

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual class LiteRtImage(val uiImage: UIImage) {
    actual fun resize(width: Int, height: Int): LiteRtImage {
        val size = CGSizeMake(width.toDouble(), height.toDouble())
        UIGraphicsBeginImageContextWithOptions(size, false, 1.0)
        uiImage.drawInRect(CGRectMake(0.0, 0.0, width.toDouble(), height.toDouble()))
        val resizedImage = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        return LiteRtImage(resizedImage!!)
    }

    actual fun toFloatArray(mean: Float, std: Float): FloatArray {
        val imageRef = uiImage.CGImage ?: throw IllegalStateException("Unable to get CGImage")
        val width = CGImageGetWidth(imageRef).toInt()
        val height = CGImageGetHeight(imageRef).toInt()
        val colorSpace = CGColorSpaceCreateDeviceRGB()
        val bytesPerPixel = 4
        val bytesPerRow = bytesPerPixel * width
        val bitsPerComponent = 8
        val pixels = nativeHeap.allocArray<ByteVar>(height * width * bytesPerPixel)

        val context = CGBitmapContextCreate(
            pixels,
            width.toULong(),
            height.toULong(),
            bitsPerComponent.toULong(),
            bytesPerRow.toULong(),
            colorSpace,
            CGImageAlphaInfo.kCGImageAlphaPremultipliedLast.value
        )

        CGContextDrawImage(context, CGRectMake(0.0, 0.0, width.toDouble(), height.toDouble()), imageRef)

        val floatArray = FloatArray(width * height * 3)
        for (i in 0 until width * height) {
            val r = (pixels[i * 4].toUByte().toFloat() - mean) / std
            val g = (pixels[i * 4 + 1].toUByte().toFloat() - mean) / std
            val b = (pixels[i * 4 + 2].toUByte().toFloat() - mean) / std
            floatArray[i * 3] = r
            floatArray[i * 3 + 1] = g
            floatArray[i * 3 + 2] = b
        }

        nativeHeap.free(pixels)
        return floatArray
    }

    actual companion object {
        actual fun fromBytes(bytes: ByteArray): LiteRtImage {
            val data = bytes.usePinned { pinned ->
                NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
            }
            val image = UIImage.imageWithData(data) ?: throw IllegalArgumentException("Failed to decode image from bytes")
            return LiteRtImage(image)
        }
    }
}
