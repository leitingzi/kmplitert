package io.github.kmplitert.dataprocess.audio

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AudioTest {
    @Test
    fun stereoResamplingKeepsChannelsSeparate() {
        val audio = LiteRtAudio(floatArrayOf(0f, 10f, 2f, 20f), 2, 2)
        assertContentEquals(floatArrayOf(0f, 10f, 1f, 15f, 2f, 20f, 2f, 20f), audio.resample(4).data)
        assertContentEquals(floatArrayOf(5f, 11f), audio.toMono().data)
        assertFailsWith<IllegalArgumentException> { LiteRtAudio(floatArrayOf(0f), 1, 2) }
        assertFailsWith<IllegalArgumentException> { audio.resample(0) }
    }

    @Test
    fun signalProcessingValidatesParameters() {
        val real = floatArrayOf(1f, 0f, 0f, 0f)
        val imag = FloatArray(4)
        SignalProcessing.fft(real, imag)
        assertContentEquals(floatArrayOf(1f, 1f, 1f, 1f), real)
        assertContentEquals(floatArrayOf(1f), SignalProcessing.hanningWindow(1))
        assertContentEquals(floatArrayOf(1f), SignalProcessing.hammingWindow(1))
        assertFailsWith<IllegalArgumentException> { SignalProcessing.fft(FloatArray(3), FloatArray(3)) }
        assertFailsWith<IllegalArgumentException> { SignalProcessing.fft(FloatArray(4), FloatArray(2)) }
        assertFailsWith<IllegalArgumentException> { SignalProcessing.powerSpectrogram(FloatArray(4), 4, 0, FloatArray(4)) }
        assertFailsWith<IllegalArgumentException> { SignalProcessing.melFilterBanks(4, 8, 16000, highFreq = 9000f) }
    }

    private fun wav(format: Int, bits: Int, data: ByteArray, extra: Boolean = false): ByteArray {
        val extraSize = if (extra) 10 else 0
        val dataPadding = data.size % 2
        val bytes = ByteArray(44 + extraSize + data.size + dataPadding)
        fun text(offset: Int, value: String) = value.encodeToByteArray().copyInto(bytes, offset)
        fun u16(offset: Int, value: Int) { bytes[offset] = value.toByte(); bytes[offset + 1] = (value shr 8).toByte() }
        fun i32(offset: Int, value: Int) { u16(offset, value); u16(offset + 2, value shr 16) }
        text(0, "RIFF"); i32(4, bytes.size - 8); text(8, "WAVE")
        text(12, "fmt "); i32(16, 16); u16(20, format); u16(22, 1)
        i32(24, 8000); i32(28, 8000 * bits / 8); u16(32, bits / 8); u16(34, bits)
        if (extra) { text(36, "JUNK"); i32(40, 1); bytes[44] = 7 }
        val dataHeader = 36 + extraSize
        text(dataHeader, "data"); i32(dataHeader + 4, data.size)
        data.copyInto(bytes, dataHeader + 8)
        return bytes
    }

    @Test
    fun wavSupportsPcmFloatAndPaddedChunks() {
        assertContentEquals(floatArrayOf(-1f, 0f, 127f / 128f),
            WavDecoder.decode(wav(1, 8, byteArrayOf(0, -128, -1), extra = true)).data)
        assertContentEquals(floatArrayOf(-1f, 0f, 32767f / 32768f),
            WavDecoder.decode(wav(1, 16, byteArrayOf(0, -128, 0, 0, -1, 127))).data)
        assertContentEquals(floatArrayOf(-1f, 0f),
            WavDecoder.decode(wav(1, 24, byteArrayOf(0, 0, -128, 0, 0, 0))).data)
        assertContentEquals(floatArrayOf(0.5f),
            WavDecoder.decode(wav(3, 32, byteArrayOf(0, 0, 0, 63))).data)
        assertContentEquals(floatArrayOf(-1f, 0f),
            WavDecoder.decode(wav(1, 32, byteArrayOf(0, 0, 0, -128, 0, 0, 0, 0))).data)
        assertEquals(8000, LiteRtAudio.fromWav(wav(1, 8, byteArrayOf(0))).sampleRate)
        assertFailsWith<IllegalArgumentException> { WavDecoder.decode(wav(6, 8, byteArrayOf(0))) }
        assertFailsWith<IllegalArgumentException> { WavDecoder.decode(wav(1, 16, byteArrayOf(0))) }
        assertFailsWith<IllegalArgumentException> { WavDecoder.decode(wav(1, 8, byteArrayOf(0)).copyOf(44)) }
    }
}
