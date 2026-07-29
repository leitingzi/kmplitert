package io.github.kmplitert.tool.gesture

import kotlin.test.*

class GestureTest {

    @Test
    fun testLandmarkDistance() {
        val p1 = HandLandmark(0f, 0f, 0f)
        val p2 = HandLandmark(3f, 4f, 0f)
        assertEquals(5f, HandLandmarkUtils.calculateDistance(p1, p2), 1e-5f)
        
        val p3 = HandLandmark(0f, 0f, 0f)
        val p4 = HandLandmark(0f, 0f, 10f)
        assertEquals(10f, HandLandmarkUtils.calculateDistance(p3, p4), 1e-5f)
    }

    @Test
    fun testPinchDetection() {
        // Mock landmarks
        val landmarks = MutableList(21) { HandLandmark(0f, 0f, 0f) }
        
        // Pinching: thumb tip (4) and index tip (8) are close
        landmarks[4] = HandLandmark(0.5f, 0.5f, 0f)
        landmarks[8] = HandLandmark(0.51f, 0.51f, 0f)
        
        val resultPinching = HandGestureResult(landmarks, emptyList())
        assertTrue(resultPinching.isPinching(0.05f), "Should detect pinch")
        
        // Not pinching
        landmarks[8] = HandLandmark(0.6f, 0.6f, 0f)
        val resultNotPinching = HandGestureResult(landmarks, emptyList())
        assertFalse(resultNotPinching.isPinching(0.05f), "Should not detect pinch")
    }

    @Test
    fun testGestureResult() {
        val gestures = listOf(
            Gesture("Pinch", 0.9f, 0),
            Gesture("Open", 0.1f, 1)
        )
        val result = HandGestureResult(emptyList(), gestures, "Right" to 0.95f)
        
        assertEquals(2, result.gestures.size)
        assertEquals("Pinch", result.gestures[0].label)
        assertEquals("Right", result.handedness.first)
    }
}
