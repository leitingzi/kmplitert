package io.github.kmplitert.core.model

import io.github.kmplitert.core.LiteRTLayout
import com.sun.jna.Platform
import com.sun.jna.Structure

open class LiteRtLayout : Structure() {
    @JvmField
    var flags: Int = 0

    @JvmField
    var padding: Int = 0

    @JvmField
    var dimensions = IntArray(8)

    @JvmField
    var strides = IntArray(8)

    override fun getFieldOrder(): List<String> {
        return listOf("flags", "padding", "dimensions", "strides")
    }

    fun getRank(): Int {
        return flags and 0x7F
    }

    fun setRank(rank: Int) {
        flags = (flags and (0x1 shl 7)) or (rank and 0x7F)
    }

    fun hasStrides(): Boolean {
        return (flags shr 7) and 0x1 == 1
    }

    fun setHasStrides(value: Boolean) {
        flags = if (value) {
            flags or (1 shl 7)
        } else {
            flags and (1 shl 7).inv()
        }
    }

    fun toPlatform(): LiteRTLayout {
        val rank = getRank()
        val dims = dimensions.sliceArray(0 until rank).toList()
        val strds = if (hasStrides()) {
            strides.sliceArray(0 until rank).toList()
        } else {
            LiteRTLayout.calculateDefaultStrides(dims)
        }
        return LiteRTLayout(dimensions = dims, strides = strds)
    }

    class ByReference : LiteRtLayout(), Structure.ByReference
    class ByValue : LiteRtLayout(), Structure.ByValue
}


