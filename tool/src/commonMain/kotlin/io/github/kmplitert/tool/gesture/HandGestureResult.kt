package io.github.kmplitert.tool.gesture

/**
 * The result of a hand gesture recognition operation.
 *
 * @param landmarks A list of 21 detected hand landmarks.
 * @param gestures A list of recognized gestures.
 * @param handedness The handedness (e.g., "Left", "Right") and its score.
 */
data class HandGestureResult(
    val landmarks: List<HandLandmark>,
    val gestures: List<Gesture>,
    val handedness: Pair<String, Float> = "Unknown" to 0f
) {
    /**
     * Heuristic detection for a pinch gesture.
     *
     * A pinch is typically detected when the distance between the thumb tip
     * and the index finger tip is below a certain threshold.
     */
    fun isPinching(threshold: Float = 0.05f): Boolean {
        if (landmarks.size < 21) return false
        val thumbTip = landmarks[4]
        val indexTip = landmarks[8]
        return HandLandmarkUtils.calculateDistance(thumbTip, indexTip) < threshold
    }
}
