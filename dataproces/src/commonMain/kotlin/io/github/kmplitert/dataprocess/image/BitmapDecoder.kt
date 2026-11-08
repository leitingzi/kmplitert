package io.github.kmplitert.dataprocess.image

object BitmapDecoder {
    fun decodeToLiteRtImage(bytes: ByteArray): LiteRtImage {
        require(bytes.size >= 54 && bytes[0] == 'B'.code.toByte() && bytes[1] == 'M'.code.toByte()) {
            "Only uncompressed 24-bit BMP images are supported on this platform"
        }

        fun int32(offset: Int): Int {
            val a = bytes[offset].toInt() and 255
            val b = (bytes[offset + 1].toInt() and 255) shl 8
            val c = (bytes[offset + 2].toInt() and 255) shl 16
            val d = (bytes[offset + 3].toInt() and 255) shl 24
            return a or b or c or d
        }

        fun int16(offset: Int): Int {
            val a = bytes[offset].toInt() and 255
            val b = (bytes[offset + 1].toInt() and 255) shl 8
            return a or b
        }

        val headerSize = int32(14)
        val offset = int32(10)
        val width = int32(18)
        val signedHeight = int32(22)

        require(headerSize >= 40 && offset.toLong() >= 14L + headerSize && signedHeight != Int.MIN_VALUE && int16(26) == 1 && int16(28) == 24 && int32(30) == 0) {
            "Unsupported BMP format"
        }

        val height = absHeight(signedHeight)
        val size = pixelCount(width, height, 3)
        val rowSize = (width.toLong() * 3 + 3) / 4 * 4

        require(offset >= 0 && offset.toLong() + rowSize * height <= bytes.size.toLong()) {
            "Truncated BMP image"
        }

        val pixels = ByteArray(size)
        for (y in 0 until height) {
            val sourceY = if (signedHeight > 0) height - 1 - y else y
            val row = (offset.toLong() + sourceY * rowSize).toInt()
            for (x in 0 until width) {
                val source = row + x * 3
                val target = (y * width + x) * 3
                pixels[target] = bytes[source + 2]
                pixels[target + 1] = bytes[source + 1]
                pixels[target + 2] = bytes[source]
            }
        }
        return LiteRtImage.fromPixels(pixels, width, height, 3)
    }

    private fun absHeight(height: Int): Int {
        return if (height < 0) -height else height
    }
}
