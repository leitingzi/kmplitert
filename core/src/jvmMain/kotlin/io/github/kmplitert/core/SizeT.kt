package io.github.kmplitert.core

import com.sun.jna.IntegerType
import com.sun.jna.Native
import com.sun.jna.ptr.ByReference

class SizeT(value: Long = 0L) : IntegerType(Native.POINTER_SIZE, value, true) {
    override fun toByte(): Byte = toLong().toByte()
    override fun toShort(): Short = toLong().toShort()

    init {
        require(value >= 0L) { "size_t values cannot be negative" }
        if (Native.POINTER_SIZE == Int.SIZE_BYTES) {
            require(value <= 0xFFFF_FFFFL) { "size_t value does not fit this platform" }
        }
    }
}

internal fun Long.asSizeT(): SizeT = SizeT(this)

class SizeTByReference : ByReference(Native.POINTER_SIZE) {
    var value: Long
        get() = if (Native.POINTER_SIZE == Long.SIZE_BYTES) {
            pointer.getLong(0)
        } else {
            pointer.getInt(0).toLong() and 0xFFFF_FFFFL
        }
        set(value) {
            require(value >= 0L) { "size_t values cannot be negative" }
            if (Native.POINTER_SIZE == Long.SIZE_BYTES) {
                pointer.setLong(0, value)
            } else {
                require(value <= 0xFFFF_FFFFL) { "size_t value does not fit this platform" }
                pointer.setInt(0, value.toInt())
            }
        }
}
