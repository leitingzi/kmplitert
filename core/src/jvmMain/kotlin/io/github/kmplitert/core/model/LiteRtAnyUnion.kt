package io.github.kmplitert.core.model

import com.sun.jna.Pointer
import com.sun.jna.Union

class LiteRtAnyUnion : Union() {
    @JvmField
    var bool_value: Byte = 0

    @JvmField
    var int_value: Long = 0

    @JvmField
    var real_value: Double = 0.0

    @JvmField
    var str_value: Pointer? = null

    @JvmField
    var ptr_value: Pointer? = null
}
