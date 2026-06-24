package com.leitz.kmplitert.model

enum class LiteRtHwAcceleratorSet(val value: Int) {
    CPU(value = 1 shl 0),
    GPU(value = 1 shl 1),
    NPU(value = 1 shl 2);
}