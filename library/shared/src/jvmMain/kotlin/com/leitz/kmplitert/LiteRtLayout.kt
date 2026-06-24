package com.leitz.kmplitert

import com.sun.jna.Structure

@Structure.FieldOrder(
    "rankAndFlags",
    "dimensions",
    "strides"
)
class LiteRtLayout : Structure() {

    @JvmField
    var rankAndFlags: Int = 0

    @JvmField
    var dimensions = IntArray(8)

    @JvmField
    var strides = IntArray(8)

    var rank: Int
        get() = rankAndFlags and 0x7F
        set(value) {
            rankAndFlags = (rankAndFlags and 0xFFFFFF80.toInt()) or (value and 0x7F)
        }

    var hasStrides: Boolean
        get() = (rankAndFlags and 0x80) != 0
        set(value) {
            rankAndFlags = if (value) rankAndFlags or 0x80 else rankAndFlags and 0x7F
        }
}