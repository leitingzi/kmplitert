@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.kmplitert.tool.audio

/**
 * Represents audio data that can be preprocessed before being fed into a LiteRT model.
 *
 * This class provides common audio operations such as resampling and converting
 * audio data into a normalized [FloatArray].
 */
expect class LiteRtAudio {

    /**
     * The sample rate of the audio in Hz.
     */
    val sampleRate: Int

    /**
     * The number of audio channels (e.g., 1 for mono, 2 for stereo).
     */
    val channels: Int

    /**
     * Returns the audio data as a [FloatArray].
     * 
     * If multiple channels exist, they are interleaved.
     */
    fun toFloatArray(): FloatArray

    /**
     * Returns a copy of this audio data resampled to the target sample rate.
     */
    fun resample(targetSampleRate: Int): LiteRtAudio

    /**
     * Converts the audio to mono if it has multiple channels.
     */
    fun toMono(): LiteRtAudio

    companion object {
        /**
         * Creates a [LiteRtAudio] instance from raw PCM float data.
         * 
         * @param data The PCM float data.
         * @param sampleRate The sample rate of the audio.
         * @param channels The number of channels.
         */
        fun fromRaw(data: FloatArray, sampleRate: Int, channels: Int): LiteRtAudio

        /**
         * Decodes a WAV file into a [LiteRtAudio] instance.
         */
        fun fromWav(bytes: ByteArray): LiteRtAudio
    }
}
