package io.github.leitingzi.kmplitert.core.model

import com.sun.jna.PointerType
import io.github.leitingzi.kmplitert.core.LiteRtLibrary

class LiteRtTensor : PointerType() {
    fun getRankedTensorType(): LiteRtRankedTensorType {
        val type = LiteRtRankedTensorType()
        val status = LiteRtLibrary.INSTANCE.LiteRtGetRankedTensorType(this, type)
        check(status == 0) {
            "Failed to get ranked tensor type: $status"
        }
        return type
    }
}
