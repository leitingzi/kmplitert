package io.github.kmplitert.core.model

import com.sun.jna.Structure
import io.github.kmplitert.core.LiteRTElementType
import io.github.kmplitert.core.LiteRTTensorType

open class LiteRtRankedTensorType : Structure() {
    @JvmField
    var elementType: Int = 0

    @JvmField
    var layout: LiteRtLayout = LiteRtLayout()

    override fun getFieldOrder(): List<String> {
        return listOf("elementType", "layout")
    }

    fun toPlatform(): LiteRTTensorType {
        return LiteRTTensorType(
            elementType = LiteRTElementType.fromNative(elementType),
            layout = layout.toPlatform()
        )
    }

    class ByReference : LiteRtRankedTensorType(), Structure.ByReference
}

