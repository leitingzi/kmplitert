package io.github.kmplitert.core.model

import com.sun.jna.Memory
import com.sun.jna.Structure

@Structure.FieldOrder("tag", "value")
open class LiteRtEnvOption : Structure() {
    @JvmField
    var tag: Int = 0

    @JvmField
    var value: LiteRtAny = LiteRtAny()

    internal var retainedMemory: Memory? = null

    internal fun copyTo(target: LiteRtEnvOption) {
        target.tag = tag
        target.value.type = value.type
        when (value.type) {
            LiteRtAnyType.BOOL -> {
                target.value.value.setType(Byte::class.java)
                target.value.value.bool_value = value.value.bool_value
            }

            LiteRtAnyType.INT -> {
                target.value.value.setType(Long::class.java)
                target.value.value.int_value = value.value.int_value
            }

            LiteRtAnyType.REAL -> {
                target.value.value.setType(Double::class.java)
                target.value.value.real_value = value.value.real_value
            }

            LiteRtAnyType.STRING, LiteRtAnyType.VOID_PTR -> {
                target.value.value.setType(com.sun.jna.Pointer::class.java)
                target.value.value.ptr_value = value.value.ptr_value
            }

            else -> {
                target.value.value.setType(Long::class.java)
                target.value.value.int_value = 0
            }
        }
        target.write()
    }

    class ByReference : LiteRtEnvOption(), Structure.ByReference
}

internal object LiteRtAnyType {
    const val NONE = 0
    const val BOOL = 1
    const val INT = 2
    const val REAL = 3
    const val STRING = 8
    const val VOID_PTR = 9
}

internal object LiteRtEnvOptionTag {
    const val COMPILER_PLUGIN_LIBRARY_DIR = 0
}
