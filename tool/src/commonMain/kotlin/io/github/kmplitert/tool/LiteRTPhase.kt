package io.github.kmplitert.tool

/**
 * Execution phases of a LiteRT task.
 */
enum class LiteRTPhase {
    /** Wraps the entire task (preprocess, inference, and postprocess). */
    TASK,
    /** Wraps the preprocessing step (transforming input and feeding data to input buffers). */
    PREPROCESS,
    /** Wraps the model inference step. */
    INFERENCE,
    /** Wraps the postprocessing step. */
    POSTPROCESS
}