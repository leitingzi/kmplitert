package io.github.leitingzi.kmplitert.tool.audio

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LiteRtAudioTest {

    @Test
    fun testAudioCreation() {
        val data = FloatArray(100) { 0.5f }
        val audio = LiteRtAudio.fromRaw(data, 16000, 1)
        
        assertEquals(16000, audio.sampleRate)
        assertEquals(1, audio.channels)
        assertEquals(100, audio.toFloatArray().size)
    }

    @Test
    fun testResample() {
        val data = FloatArray(100) { 0.5f }
        val audio = LiteRtAudio.fromRaw(data, 1000, 1)
        val resampled = audio.resample(2000)
        
        assertEquals(2000, resampled.sampleRate)
        assertEquals(200, resampled.toFloatArray().size)
    }

    @Test
    fun testToMono() {
        // Stereo: LRLR...
        val data = FloatArray(100) { i -> if (i % 2 == 0) 1f else 0f }
        val audio = LiteRtAudio.fromRaw(data, 16000, 2)
        val mono = audio.toMono()
        
        assertEquals(1, mono.channels)
        assertEquals(50, mono.toFloatArray().size)
        assertEquals(0.5f, mono.toFloatArray()[0])
    }
}
