package io.github.kmplitert.tool.pose

/**
 * The result of a pose tracking operation.
 *
 * @param landmarks A list of detected pose landmarks.
 */
data class PoseResult(
    val landmarks: List<PoseLandmark>
)
