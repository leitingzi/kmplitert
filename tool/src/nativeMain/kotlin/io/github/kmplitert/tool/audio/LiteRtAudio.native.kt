@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.kmplitert.tool.audio

actual class LiteRtAudio(
    private val data: FloatArray,
    actual val sampleRate: Int,
    actual val channels: Int
) {
    actual fun toFloatArray(): FloatArray = data

    actual fun resample(targetSampleRate: Int): LiteRtAudio {
        if (targetSampleRate == sampleRate) return this
        val ratio = targetSampleRate.toDouble() / sampleRate
        val newSize = (data.size * ratio).toInt()
        val newData = FloatArray(newSize)
        for (i in 0 until newSize) {
            val originalIndex = i / ratio
            val index = originalIndex.toInt()
            val fraction = (originalIndex - index).toFloat()
            if (index + 1 < data.size) {
                newData[i] = data[index] * (1 - fraction) + data[index + 1] * fraction
            } else {
                newData[i] = data[index]
            }
        }
        return LiteRtAudio(newData, targetSampleRate, channels)
    }

    actual fun toMono(): LiteRtAudio {
        if (channels == 1) return this
        val newData = FloatArray(data.size / channels)
        for (i in newData.indices) {
            var sum = 0f
            for (c in 0 until channels) {
                sum += data[i * channels + c]
            }
            newData[i] = sum / channels
        }
        return LiteRtAudio(newData, sampleRate, 1)
    }

    actual companion object {
        actual fun fromRaw(data: FloatArray, sampleRate: Int, channels: Int): LiteRtAudio {
            return LiteRtAudio(data, sampleRate, channels)
        }

        actual fun fromWav(bytes: ByteArray): LiteRtAudio {
            val info = WavDecoder.decode(bytes)
            return LiteRtAudio(info.data, info.sampleRate, info.channels)
        }
    }
}
