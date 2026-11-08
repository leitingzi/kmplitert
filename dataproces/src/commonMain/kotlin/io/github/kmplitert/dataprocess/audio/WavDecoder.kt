package io.github.kmplitert.dataprocess.audio

/** Decoder for little-endian PCM (8/16/24/32-bit) and IEEE float (32-bit) WAV files. */
object WavDecoder {
    class WavInfo(val data: FloatArray, val sampleRate: Int, val channels: Int)

    fun decode(bytes: ByteArray): WavInfo {
        require(bytes.size >= 12 && bytes.decodeToString(0, 4) == "RIFF" &&
            bytes.decodeToString(8, 12) == "WAVE") { "Invalid RIFF/WAVE header" }
        fun u16(offset: Int) = (bytes[offset].toInt() and 255) or ((bytes[offset + 1].toInt() and 255) shl 8)
        fun i32(offset: Int) = (bytes[offset].toInt() and 255) or ((bytes[offset + 1].toInt() and 255) shl 8) or
            ((bytes[offset + 2].toInt() and 255) shl 16) or (bytes[offset + 3].toInt() shl 24)
        val end = 8L + (i32(4).toLong() and 0xffffffffL)
        require(end in 12L..bytes.size.toLong()) { "Truncated RIFF data" }
        var offset = 12
        var format = 0
        var channels = 0
        var sampleRate = 0
        var bits = 0
        var blockAlign = 0
        var dataOffset = -1
        var dataSize = 0
        while (offset.toLong() + 8 <= end) {
            val id = bytes.decodeToString(offset, offset + 4)
            val size = i32(offset + 4).toLong() and 0xffffffffL
            val start = offset.toLong() + 8
            val next = start + size + (size and 1)
            require(next <= end) { "Truncated WAV chunk: $id" }
            if (id == "fmt ") {
                require(size >= 16) { "Invalid WAV format chunk" }
                val index = start.toInt()
                format = u16(index)
                channels = u16(index + 2)
                sampleRate = i32(index + 4)
                blockAlign = u16(index + 12)
                bits = u16(index + 14)
            } else if (id == "data" && dataOffset < 0) {
                dataOffset = start.toInt()
                dataSize = size.toInt()
            }
            offset = next.toInt()
        }
        require(offset.toLong() == end) { "Incomplete WAV chunk header" }
        require(channels > 0 && sampleRate > 0 && dataOffset >= 0) { "Missing or invalid WAV format/data" }
        require((format == 1 && bits in listOf(8, 16, 24, 32)) || (format == 3 && bits == 32)) {
            "Unsupported WAV format: $format, $bits bits"
        }
        val bytesPerSample = bits / 8
        require(blockAlign == channels * bytesPerSample && dataSize % blockAlign == 0) { "Invalid WAV block alignment" }
        val data = FloatArray(dataSize / bytesPerSample) { i ->
            val pos = dataOffset + i * bytesPerSample
            when {
                format == 3 -> Float.fromBits(i32(pos))
                bits == 8 -> ((bytes[pos].toInt() and 255) - 128) / 128f
                bits == 16 -> u16(pos).toShort().toInt() / 32768f
                bits == 24 -> ((bytes[pos].toInt() and 255) or ((bytes[pos + 1].toInt() and 255) shl 8) or
                    (bytes[pos + 2].toInt() shl 16)) / 8388608f
                else -> (i32(pos).toDouble() / 2147483648.0).toFloat()
            }
        }
        return WavInfo(data, sampleRate, channels)
    }
}
