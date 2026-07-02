package io.github.leitingzi.kmplitert.core

/**
 * Hardware accelerator used for LiteRT inference.
 */
enum class LiteRTAccelerator {

    /** Runs inference on the CPU. */
    CPU,

    /** Runs inference on the GPU. */
    GPU,

    /** Runs inference on the NPU or dedicated AI accelerator. */
    NPU;
}

