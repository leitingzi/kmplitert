package io.github.kmplitert.tool.audio

/**
 * A simple WAV file decoder.
 */
object WavDecoder {

    class WavInfo(val data: FloatArray, val sampleRate: Int, val channels: Int)

    fun decode(bytes: ByteArray): WavInfo {
        // Simple RIFF WAV parser
        // Bytes 0-3: "RIFF"
        // Bytes 8-11: "WAVE"
        // Bytes 12-15: "fmt "
        // Bytes 22: Num Channels (2 bytes)
        // Bytes 24: Sample Rate (4 bytes)
        // Bytes 34: Bits Per Sample (2 bytes)
        
        if (bytes.size < 44) throw IllegalArgumentException("Invalid WAV file")
        
        val riff = bytes.decodeToString(0, 4)
        if (riff != "RIFF") throw IllegalArgumentException("Not a RIFF file")
        
        val wave = bytes.decodeToString(8, 12)
        if (wave != "WAVE") throw IllegalArgumentException("Not a WAVE file")
        
        var offset = 12
        var channels = 0
        var sampleRate = 0
        var bitsPerSample = 0
        var data: FloatArray? = null
        
        while (offset + 8 <= bytes.size) {
            val chunkId = bytes.decodeToString(offset, offset + 4)
            val chunkSize = readInt(bytes, offset + 4)
            offset += 8
            
            when (chunkId) {
                "fmt " -> {
                    channels = readShort(bytes, offset + 2)
                    sampleRate = readInt(bytes, offset + 4)
                    bitsPerSample = readShort(bytes, offset + 14)
                }
                "data" -> {
                    val numSamples = chunkSize / (bitsPerSample / 8)
                    data = FloatArray(numSamples)
                    for (i in 0 until numSamples) {
                        val sampleOffset = offset + i * (bitsPerSample / 8)
                        data[i] = when (bitsPerSample) {
                            16 -> readShort(bytes, sampleOffset).toFloat() / 32768f
                            8 -> (bytes[sampleOffset].toInt() and 0xFF - 128).toFloat() / 128f
                            32 -> readInt(bytes, sampleOffset).toFloat() / 2.1474836E9f
                            else -> 0f
                        }
                    }
                }
            }
            offset += chunkSize
        }
        
        if (data == null) throw IllegalArgumentException("No data chunk found in WAV")
        
        return WavInfo(data, sampleRate, channels)
    }

    private fun readInt(bytes: ByteArray, offset: Int): Int {
        return (bytes[offset].toInt() and 0xFF) or
               ((bytes[offset + 1].toInt() and 0xFF) shl 8) or
               ((bytes[offset + 2].toInt() and 0xFF) shl 16) or
               ((bytes[offset + 3].toInt() and 0xFF) shl 24)
    }

    private fun readShort(bytes: ByteArray, offset: Int): Int {
        val result = (bytes[offset].toInt() and 0xFF) or
                     ((bytes[offset + 1].toInt() and 0xFF) shl 8)
        return if (result > 32767) result - 65536 else result
    }
}
