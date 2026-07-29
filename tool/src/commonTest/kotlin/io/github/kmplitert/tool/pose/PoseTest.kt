package io.github.kmplitert.tool.pose

import kotlin.test.Test
import kotlin.test.assertEquals

class PoseTest {

    @Test
    fun testPoseResult() {
        val landmarks = listOf(
            PoseLandmark(0.5f, 0.5f, 0f, 0.9f),
            PoseLandmark(0.6f, 0.6f, 0.1f, 0.85f)
        )

        val result = PoseResult(landmarks)
        assertEquals(2, result.landmarks.size)
        assertEquals(0.5f, result.landmarks[0].x)
        assertEquals(0.9f, result.landmarks[0].score)
    }

    @Test
    fun testPoseLandmarkIndices() {
        assertEquals(0, PoseLandmarkIndices.MoveNet.NOSE)
        assertEquals(5, PoseLandmarkIndices.MoveNet.LEFT_SHOULDER)
        assertEquals(12, PoseLandmarkIndices.BlazePose.RIGHT_SHOULDER)
        assertEquals(32, PoseLandmarkIndices.BlazePose.RIGHT_FOOT_INDEX)
    }
}