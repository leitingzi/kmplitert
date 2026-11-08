package io.github.kmplitert.dataprocess.image

internal fun yuvToRgb(y: Int, u: Int, v: Int, fullRange: Boolean = true): Int {
    val luminance = if (fullRange) y.toFloat() else (y - 16).coerceAtLeast(0) * (255f / 219f)
    val chromaScale = if (fullRange) 1f else 255f / 224f
    val cb = (u - 128) * chromaScale
    val cr = (v - 128) * chromaScale
    val red = (luminance + 1.402f * cr).toInt().coerceIn(0, 255)
    val green = (luminance - 0.344136f * cb - 0.714136f * cr).toInt().coerceIn(0, 255)
    val blue = (luminance + 1.772f * cb).toInt().coerceIn(0, 255)
    return (red shl 16) or (green shl 8) or blue
}
