package io.github.kmplitert.tool

/**
 * Execution phases of a LiteRT task.
 */
enum class LiteRTPhase {
    /** Wraps the entire task (transform, feed, inference, and postprocess). */
    TASK,
    /** Wraps the data transformation step (e.g., Image -> FloatArray). */
    TRANSFORM,
    /** Wraps the data feeding step (writing data to input buffers). */
    FEED,
    /** Wraps the model inference step. */
    INFERENCE,
    /** Wraps the postprocessing step. */
    POSTPROCESS
}