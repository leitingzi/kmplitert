package io.github.leitingzi.kmplitert.tool.audio

import kotlin.math.*

/**
 * Utility functions for audio signal processing.
 */
object SignalProcessing {

    /**
     * Performs an In-place Fast Fourier Transform.
     * 
     * @param real The real part of the signal. Must have a length that is a power of 2.
     * @param imag The imaginary part of the signal. Must have a same length as [real].
     */
    fun fft(real: FloatArray, imag: FloatArray) {
        val n = real.size
        if (n <= 1) return
        
        // Bit-reversal permutation
        var j = 0
        for (i in 0 until n) {
            if (i < j) {
                val tempReal = real[i]
                real[i] = real[j]
                real[j] = tempReal
                val tempImag = imag[i]
                imag[i] = imag[j]
                imag[j] = tempImag
            }
            var m = n shr 1
            while (m in 1..j) {
                j -= m
                m = m shr 1
            }
            j += m
        }

        // Cooley-Tukey FFT
        var len = 2
        while (len <= n) {
            val angle = -2.0 * PI / len
            val wLenReal = cos(angle).toFloat()
            val wLenImag = sin(angle).toFloat()
            for (i in 0 until n step len) {
                var wReal = 1f
                var wImag = 0f
                for (k in 0 until len / 2) {
                    val uReal = real[i + k]
                    val uImag = imag[i + k]
                    val vReal = real[i + k + len / 2] * wReal - imag[i + k + len / 2] * wImag
                    val vImag = real[i + k + len / 2] * wImag + imag[i + k + len / 2] * wReal
                    real[i + k] = uReal + vReal
                    imag[i + k] = uImag + vImag
                    real[i + k + len / 2] = uReal - vReal
                    imag[i + k + len / 2] = uImag - vImag
                    
                    val nextWReal = wReal * wLenReal - wImag * wLenImag
                    wImag = wReal * wLenImag + wImag * wLenReal
                    wReal = nextWReal
                }
            }
            len = len shl 1
        }
    }

    /**
     * Applies a Hanning window to the signal.
     */
    @Suppress("SpellCheckingInspection")
    fun hanningWindow(size: Int): FloatArray {
        return FloatArray(size) { i ->
            0.5f * (1f - cos(2f * PI.toFloat() * i / (size - 1)))
        }
    }

    /**
     * Applies a Hamming window to the signal.
     */
    fun hammingWindow(size: Int): FloatArray {
        return FloatArray(size) { i ->
            0.54f - 0.46f * cos(2f * PI.toFloat() * i / (size - 1))
        }
    }

    /**
     * Converts a frequency in Hertz to Mel scale.
     */
    fun hzToMel(hz: Float): Float {
        return 2595f * log10(1f + hz / 700f)
    }

    /**
     * Converts a Mel scale value back to frequency in Hertz.
     */
    fun melToHz(mel: Float): Float {
        return 700f * (10f.pow(mel / 2595f) - 1f)
    }

    /**
     * Generates Mel filter banks.
     */
    fun melFilterBanks(
        numFilters: Int,
        fftSize: Int,
        sampleRate: Int,
        lowFreq: Float = 0f,
        highFreq: Float = sampleRate / 2f
    ): Array<FloatArray> {
        val lowMel = hzToMel(lowFreq)
        val highMel = hzToMel(highFreq)
        val melPoints = FloatArray(numFilters + 2) { i ->
            melToHz(lowMel + (highMel - lowMel) * i / (numFilters + 1))
        }
        
        val binPoints = IntArray(numFilters + 2) { i ->
            floor((fftSize + 1) * melPoints[i] / sampleRate).toInt()
        }
        
        val filters = Array(numFilters) { i ->
            FloatArray(fftSize / 2 + 1) { j ->
                when {
                    j < binPoints[i] -> 0f
                    j >= binPoints[i] && j < binPoints[i + 1] -> (j - binPoints[i]).toFloat() / (binPoints[i + 1] - binPoints[i])
                    j >= binPoints[i + 1] && j < binPoints[i + 2] -> (binPoints[i + 2] - j).toFloat() / (binPoints[i + 2] - binPoints[i + 1])
                    else -> 0f
                }
            }
        }
        return filters
    }

    /**
     * Computes the power spectrogram of a signal.
     */
    fun powerSpectrogram(
        signal: FloatArray,
        fftSize: Int,
        hopSize: Int,
        window: FloatArray
    ): List<FloatArray> {
        val spectrogram = mutableListOf<FloatArray>()
        var offset = 0
        while (offset + fftSize <= signal.size) {
            val frame = FloatArray(fftSize) { i -> signal[offset + i] * window[i] }
            val imag = FloatArray(fftSize)
            fft(frame, imag)
            
            val powerFrame = FloatArray(fftSize / 2 + 1) { i ->
                (frame[i] * frame[i] + imag[i] * imag[i])
            }
            spectrogram.add(powerFrame)
            offset += hopSize
        }
        return spectrogram
    }

    /**
     * Computes the Mel-spectrogram of a signal.
     */
    fun melSpectrogram(
        signal: FloatArray,
        sampleRate: Int,
        fftSize: Int,
        hopSize: Int,
        numMels: Int,
        window: FloatArray = hanningWindow(fftSize)
    ): List<FloatArray> {
        val powerSpec = powerSpectrogram(signal, fftSize, hopSize, window)
        val filters = melFilterBanks(numMels, fftSize, sampleRate)
        
        return powerSpec.map { frame ->
            FloatArray(numMels) { i ->
                var sum = 0f
                for (j in frame.indices) {
                    sum += frame[j] * filters[i][j]
                }
                sum
            }
        }
    }
}
