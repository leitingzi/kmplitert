package io.github.kmplitert.tool.gesture

import kotlin.math.sqrt

/**
 * Utility functions for hand landmark calculations.
 */
object HandLandmarkUtils {
    /**
     * Calculates the Euclidean distance between two landmarks in 3D space.
     */
    fun calculateDistance(p1: HandLandmark, p2: HandLandmark): Float {
        val dx = p1.x - p2.x
        val dy = p1.y - p2.y
        val dz = p1.z - p2.z
        return sqrt(dx * dx + dy * dy + dz * dz)
    }

    /**
     * MediaPipe Hand Landmark Indices for reference:
     * 0: WRIST
     * 1: THUMB_CMC
     * 2: THUMB_MCP
     * 3: THUMB_IP
     * 4: THUMB_TIP
     * 5: INDEX_FINGER_MCP
     * 6: INDEX_FINGER_PIP
     * 7: INDEX_FINGER_DIP
     * 8: INDEX_FINGER_TIP
     * 9: MIDDLE_FINGER_MCP
     * 10: MIDDLE_FINGER_PIP
     * 11: MIDDLE_FINGER_DIP
     * 12: MIDDLE_FINGER_TIP
     * 13: RING_FINGER_MCP
     * 14: RING_FINGER_PIP
     * 15: RING_FINGER_DIP
     * 16: RING_FINGER_TIP
     * 17: PINKY_MCP
     * 18: PINKY_PIP
     * 19: PINKY_DIP
     * 20: PINKY_TIP
     */
}
