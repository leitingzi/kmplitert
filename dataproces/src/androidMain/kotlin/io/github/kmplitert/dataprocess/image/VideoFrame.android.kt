package io.github.kmplitert.dataprocess.image

import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.media.Image
import java.nio.ByteBuffer

internal actual fun decodeVideoFrame(frame: Any): LiteRtImage = when (frame) {
    is Bitmap -> frame.asLiteRtImage()
    is Image -> LiteRtImage.fromAndroidImage(frame)
    else -> {
        val method = try {
            frame.javaClass.getMethod("getImage")
        } catch (e: NoSuchMethodException) {
            throw IllegalArgumentException("Unsupported frame type: ${frame.javaClass.name}", e)
        }
        val image = try {
            method.invoke(frame) as? Image
        } catch (e: ReflectiveOperationException) {
            throw IllegalArgumentException("Unable to read image from ${frame.javaClass.name}", e)
        }
        requireNotNull(image) { "Frame does not contain an Android Image" }
        LiteRtImage.fromAndroidImage(image)
    }
}

fun LiteRtImage.Companion.fromAndroidImage(
    image: Image,
    rotation: ImageRotation = ImageRotation.ROTATION_0,
    flip: ImageFlip = ImageFlip(),
): LiteRtImage {
    require(image.format == ImageFormat.YUV_420_888) { "Only YUV_420_888 images are supported" }
    val planes = image.planes
    require(planes.size == 3) { "YUV image requires three planes" }
    val rgb = fromYuv420Planes(image.width, image.height, planes.map {
        YuvPlane(it.buffer.duplicate(), it.rowStride, it.pixelStride)
    })
    return rgb.transformFrame(rotation, flip)
}

internal data class YuvPlane(val buffer: ByteBuffer, val rowStride: Int, val pixelStride: Int)

internal fun fromYuv420Planes(width: Int, height: Int, planes: List<YuvPlane>): LiteRtImage {
    val pixels = ByteArray(pixelCount(width, height, 3))
    require(planes.size == 3 && planes.all { it.rowStride > 0 && it.pixelStride > 0 }) {
        "YUV image requires three valid planes"
    }
    fun sample(plane: Int, x: Int, y: Int): Int {
        val info = planes[plane]
        val buffer = info.buffer
        val index = buffer.position().toLong() + y.toLong() * info.rowStride + x.toLong() * info.pixelStride
        require(index < buffer.limit()) { "Truncated YUV plane $plane" }
        return buffer.get(index.toInt()).toInt() and 255
    }
    for (y in 0 until height) for (x in 0 until width) {
        val rgb = yuvToRgb(sample(0, x, y), sample(1, x / 2, y / 2), sample(2, x / 2, y / 2))
        val offset = (y * width + x) * 3
        pixels[offset] = (rgb shr 16).toByte()
        pixels[offset + 1] = (rgb shr 8).toByte()
        pixels[offset + 2] = rgb.toByte()
    }
    return LiteRtImage.fromPixels(pixels, width, height, 3)
}

fun LiteRtImage.asBitmap(): Bitmap {
    val pixels = data
    val colors = IntArray(width * height)
    for (i in colors.indices) {
        val offset = i * channels
        val r = pixels[offset].toInt() and 255
        val g = pixels[offset + if (channels == 1) 0 else 1].toInt() and 255
        val b = pixels[offset + if (channels == 1) 0 else 2].toInt() and 255
        val a = if (channels == 4) pixels[offset + 3].toInt() and 255 else 255
        colors[i] = (a shl 24) or (r shl 16) or (g shl 8) or b
    }
    return Bitmap.createBitmap(colors, width, height, Bitmap.Config.ARGB_8888)
}
