package io.github.leitingzi.model

enum class LiteRtHwAcceleratorSet(val value: Int) {
    CPU(value = 1 shl 0),

    /**
     * GPU hardware acceleration
     */
    GPU(value = 1 shl 1),

    /**
     * NPU hardware acceleration
     */
    NPU(value = 1 shl 2);
}