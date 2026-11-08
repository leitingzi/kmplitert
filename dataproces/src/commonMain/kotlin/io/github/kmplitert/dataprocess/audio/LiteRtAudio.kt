package io.github.kmplitert.dataprocess.audio

/**
 * Represents audio data that can be preprocessed before being fed into a LiteRT model.
 *
 * This class provides common audio operations such as resampling and converting
 * audio data into a normalized [FloatArray].
 */
class LiteRtAudio(
    val data: FloatArray,
    val sampleRate: Int,
    val channels: Int,
) {
    init {
        require(sampleRate > 0 && channels > 0 && data.size % channels == 0) {
            "Audio must have a positive sample rate and complete interleaved frames"
        }
    }

    /**
     * Returns a copy of this audio data resampled to the target sample rate.
     */
    fun resample(targetSampleRate: Int): LiteRtAudio {
        require(targetSampleRate > 0) { "Target sample rate must be positive" }
        if (targetSampleRate == sampleRate) return this
        val frames = data.size / channels
        val outputFrames = frames.toLong() * targetSampleRate / sampleRate
        require(outputFrames <= Int.MAX_VALUE / channels) { "Resampled audio is too large" }
        val output = FloatArray(outputFrames.toInt() * channels)
        for (frame in 0 until outputFrames.toInt()) {
            val position = frame.toDouble() * sampleRate / targetSampleRate
            val index = position.toInt()
            val next = (index + 1).coerceAtMost(frames - 1)
            val fraction = (position - index).toFloat()
            for (channel in 0 until channels) {
                output[frame * channels + channel] =
                    data[index * channels + channel] * (1f - fraction) + data[next * channels + channel] * fraction
            }
        }
        return LiteRtAudio(output, targetSampleRate, channels)
    }

    /**
     * Converts the audio to mono if it has multiple channels.
     */
    fun toMono(): LiteRtAudio {
        if (channels == 1) return this
        val output = FloatArray(data.size / channels)
        for (frame in output.indices) {
            var sum = 0f
            for (channel in 0 until channels) sum += data[frame * channels + channel]
            output[frame] = sum / channels
        }
        return LiteRtAudio(output, sampleRate, 1)
    }

    companion object
}

/**
 * Decodes a WAV file into a [LiteRtAudio] instance.
 */
fun LiteRtAudio.Companion.fromWav(bytes: ByteArray): LiteRtAudio {
    val info = WavDecoder.decode(bytes = bytes)
    return LiteRtAudio(info.data, info.sampleRate, info.channels)
}
