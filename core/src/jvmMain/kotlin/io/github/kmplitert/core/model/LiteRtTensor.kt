package io.github.kmplitert.core.model

import com.sun.jna.PointerType
import com.sun.jna.ptr.PointerByReference
import io.github.kmplitert.core.LiteRtLibrary
import io.github.kmplitert.core.checkLiteRtStatus

class LiteRtTensor : PointerType() {
    fun getRankedTensorType(): LiteRtRankedTensorType {
        val type = LiteRtRankedTensorType()
        val status = LiteRtLibrary.INSTANCE.LiteRtGetRankedTensorType(this, type)
        checkLiteRtStatus(status, "LiteRtGetRankedTensorType")
        return type
    }

    fun getName(): String {
        val ref = PointerByReference()
        val status = LiteRtLibrary.INSTANCE.LiteRtGetTensorName(this, ref)
        checkLiteRtStatus(status, "LiteRtGetTensorName")
        return ref.value.getString(0, "UTF-8")
    }
}
