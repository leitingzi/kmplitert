package io.github.leitingzi.kmplitert.tool.audio

/**
 * Configuration options for audio classification.
 *
 * @property topK The maximum number of top classification results to return. If <= 0, all results are returned.
 * @property scoreThreshold Results with a score below this threshold will be discarded.
 * @property labels An optional list of labels corresponding to the output tensor indices.
 * @property useMelSpectrogram Whether the model expects a Mel-spectrogram as input instead of raw PCM.
 * @property fftSize FFT size for spectrogram generation.
 * @property hopSize Hop size for spectrogram generation.
 * @property numMels Number of Mel filter banks.
 */
data class AudioClassifierOptions(
    val topK: Int = -1,
    val scoreThreshold: Float = 0f,
    val labels: List<String>? = null,
    val useMelSpectrogram: Boolean = false,
    val fftSize: Int = 1024,
    val hopSize: Int = 512,
    val numMels: Int = 64
)
