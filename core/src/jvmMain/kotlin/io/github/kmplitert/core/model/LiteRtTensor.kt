package io.github.kmplitert.core.model

import com.sun.jna.PointerType
import com.sun.jna.ptr.PointerByReference
import io.github.kmplitert.core.LiteRtLibrary

class LiteRtTensor : PointerType() {
    fun getRankedTensorType(): LiteRtRankedTensorType {
        val type = LiteRtRankedTensorType()
        val status = LiteRtLibrary.INSTANCE.LiteRtGetRankedTensorType(this, type)
        check(status == 0) {
            "Failed to get ranked tensor type: $status"
        }
        return type
    }

    fun getName(): String {
        val ref = PointerByReference()
        val status = LiteRtLibrary.INSTANCE.LiteRtGetTensorName(this, ref)
        check(status == 0) {
            "Failed to get tensor name: $status"
        }
        return ref.value.getString(0)
    }
}


