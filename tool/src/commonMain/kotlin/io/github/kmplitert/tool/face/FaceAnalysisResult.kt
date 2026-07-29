package io.github.kmplitert.tool.face

import io.github.kmplitert.tool.detection.BoundingBox

/**
 * The result of a comprehensive face analysis operation.
 *
 * @param boundingBox The detected bounding box of the face.
 * @param landmarks A list of detected face landmarks (e.g., 468 for Face Mesh).
 * @param orientation The estimated 3D orientation of the face.
 * @param expressions A list of recognized expressions/emotions.
 */
data class FaceAnalysisResult(
    val boundingBox: BoundingBox? = null,
    val landmarks: List<FaceLandmark> = emptyList(),
    val orientation: FaceOrientation? = null,
    val expressions: List<FaceExpression> = emptyList()
)
