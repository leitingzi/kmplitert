package io.github.leitingzi.kmplitert.core.model

import com.sun.jna.Structure

@Structure.FieldOrder("type", "value")
open class LiteRtAny : Structure() {
    @JvmField
    var type: Int = 0

    @JvmField
    var value: LiteRtAnyUnion = LiteRtAnyUnion()
}

