package io.github.kmplitert.tool.face

import io.github.kmplitert.tool.detection.BoundingBox
import kotlin.test.*

class FaceTest {

    @Test
    fun testFaceAnalysisResult() {
        val landmarks = listOf(
            FaceLandmark(0.5f, 0.5f, 0f),
            FaceLandmark(0.6f, 0.6f, 0.1f)
        )
        val orientation = FaceOrientation(10f, 20f, 5f)
        val expressions = listOf(FaceExpression("Happy", 0.95f))
        val box = BoundingBox(100f, 100f, 200f, 200f)

        val result = FaceAnalysisResult(
            boundingBox = box,
            landmarks = landmarks,
            orientation = orientation,
            expressions = expressions
        )

        assertEquals(box, result.boundingBox)
        assertEquals(2, result.landmarks.size)
        assertEquals(0.5f, result.landmarks[0].x)
        assertEquals(10f, result.orientation?.pitch)
        assertEquals("Happy", result.expressions[0].label)
        assertEquals(0.95f, result.expressions[0].score)
    }

    @Test
    fun testFaceAnalyzerOptions() {
        val options = FaceAnalyzerOptions(
            scoreThreshold = 0.7f,
            expressionLabels = listOf("Neutral", "Happy")
        )
        assertEquals(0.7f, options.scoreThreshold)
        assertEquals("Happy", options.expressionLabels?.get(1))
    }
}
