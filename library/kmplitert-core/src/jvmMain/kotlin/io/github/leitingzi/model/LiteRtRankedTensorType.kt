package io.github.leitingzi.model

import com.sun.jna.Structure

open class LiteRtRankedTensorType : Structure() {
    @JvmField
    var elementType: Int = 0

    @JvmField
    var layout: LiteRtLayout = LiteRtLayout()

    override fun getFieldOrder(): List<String> {
        return listOf("elementType", "layout")
    }

    class ByReference : LiteRtRankedTensorType(), Structure.ByReference
}
