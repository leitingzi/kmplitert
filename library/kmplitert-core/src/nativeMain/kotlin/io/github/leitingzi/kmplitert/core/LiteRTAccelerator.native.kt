@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
package io.github.leitingzi.kmplitert.core

import litert.*

fun LiteRTAccelerator.toNative(): LiteRtHwAcceleratorSet {
    return when (this) {
        LiteRTAccelerator.CPU -> kLiteRtHwAcceleratorCpu
        LiteRTAccelerator.GPU -> kLiteRtHwAcceleratorGpu
        LiteRTAccelerator.NPU -> kLiteRtHwAcceleratorNpu
    }
}
