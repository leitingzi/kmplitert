package com.leitz.kmplitert.model

enum class LiteRtHwAcceleratorSet(val value: Int) {
    CPU(value = 1 shl 0),

    /**
     * GPU hardware acceleration
     */
    @Deprecated(
        message = "Currently, the JVM platform does not support GPU acceleration.",
        replaceWith = ReplaceWith("LiteRtHwAcceleratorSet.CPU"),
        level = DeprecationLevel.ERROR
    )
    GPU(value = 1 shl 1),

    /**
     * CPU hardware acceleration
     */
    @Deprecated(
        message = "Currently, the JVM platform does not support NPU acceleration.",
        replaceWith = ReplaceWith("LiteRtHwAcceleratorSet.CPU"),
        level = DeprecationLevel.ERROR
    )
    NPU(value = 1 shl 2);
}