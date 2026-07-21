package io.github.kmplitert.core.model

import io.github.kmplitert.core.LiteRTElementType
import io.github.kmplitert.core.LiteRTTensorType
import com.sun.jna.Structure

open class LiteRtRankedTensorType : Structure() {
    @JvmField
    var elementType: Int = 0

    @JvmField
    var layout: LiteRtLayout = LiteRtLayout()

    override fun getFieldOrder(): List<String> {
        return listOf("elementType", "layout")
    }

    fun toPlatform(): LiteRTTensorType {
        val platformType = when (elementType) {
            1 -> LiteRTElementType.FLOAT
            2 -> LiteRTElementType.INT
            4 -> LiteRTElementType.INT64
            6 -> LiteRTElementType.BOOLEAN
            9 -> LiteRTElementType.INT8
            else -> LiteRTElementType.FLOAT // Default or error handling
        }
        return LiteRTTensorType(
            elementType = platformType,
            layout = layout.toPlatform()
        )
    }

    class ByReference : LiteRtRankedTensorType(), Structure.ByReference
}


