@file:OptIn(ExperimentalForeignApi::class)

package io.github.leitingzi.kmplitert.core

import kotlinx.cinterop.ExperimentalForeignApi
import litert.LiteRtHwAcceleratorSet
import litert.kLiteRtHwAcceleratorCpu
import litert.kLiteRtHwAcceleratorGpu
import litert.kLiteRtHwAcceleratorNpu

fun LiteRTAccelerator.toNative(): LiteRtHwAcceleratorSet {
    return when (this) {
        LiteRTAccelerator.CPU -> kLiteRtHwAcceleratorCpu
        LiteRTAccelerator.GPU -> kLiteRtHwAcceleratorGpu
        LiteRTAccelerator.NPU -> kLiteRtHwAcceleratorNpu
    }
}


