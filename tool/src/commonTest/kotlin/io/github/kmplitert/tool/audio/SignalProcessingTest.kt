package io.github.kmplitert.tool.audio

import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SignalProcessingTest {

    @Test
    fun testFft() {
        val size = 8
        val real = FloatArray(size) { i -> sin(2.0 * PI * i / size).toFloat() }
        val imag = FloatArray(size)
        
        SignalProcessing.fft(real, imag)
        
        // In index 1, we should have a peak (fundamental frequency)
        // For a sine wave sin(2*pi*k*i/N), the FFT has peaks at k and N-k
        // Here k=1, N=8. Peaks at 1 and 7.
        val magnitude1 = sqrt(real[1] * real[1] + imag[1] * imag[1])
        val magnitude7 = sqrt(real[7] * real[7] + imag[7] * imag[7])
        
        assertTrue(magnitude1 > 3.0f)
        assertTrue(magnitude7 > 3.0f)
        
        // Others should be near zero
        for (i in listOf(0, 2, 3, 4, 5, 6)) {
            val mag = sqrt(real[i] * real[i] + imag[i] * imag[i])
            assertTrue(mag < 0.1f)
        }
    }

    @Test
    fun testHzMelConversion() {
        val hz = 1000f
        val mel = SignalProcessing.hzToMel(hz)
        val convertedHz = SignalProcessing.melToHz(mel)
        
        assertEquals(hz, convertedHz, 0.1f)
    }

    @Test
    fun testMelFilterBanks() {
        val numFilters = 10
        val fftSize = 512
        val sampleRate = 16000
        
        val filters = SignalProcessing.melFilterBanks(numFilters, fftSize, sampleRate)
        
        assertEquals(numFilters, filters.size)
        assertEquals(fftSize / 2 + 1, filters[0].size)
    }
}
