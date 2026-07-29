package io.github.kmplitert.tool.pose

/**
 * Configuration options for [PoseTracker].
 *
 * @param minScoreThreshold The minimum score for a joint to be considered valid.
 * @param mean The mean value for input normalization.
 * @param std The standard deviation for input normalization.
 */
data class PoseTrackerOptions(
    val minScoreThreshold: Float = 0.2f,
    val mean: Float = 0f,
    val std: Float = 255f
)
