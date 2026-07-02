package io.github.leitingzi.kmplitert.core.model

import com.sun.jna.Pointer
import com.sun.jna.Union

class LiteRtAnyUnion : Union() {
    @JvmField
    var bool_value: Boolean = false

    @JvmField
    var int_value: Long = 0

    @JvmField
    var real_value: Double = 0.0

    @JvmField
    var str_value: String? = null

    @JvmField
    var ptr_value: Pointer? = null
}

