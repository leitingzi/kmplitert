package com.leitz.kmplitert.model

import com.leitz.kmplitert.newApi.LiteRtLibrary
import com.sun.jna.PointerType
import com.sun.jna.ptr.PointerByReference

class LiteRtModel: PointerType() {
    fun destroy() {
        LiteRtLibrary.INSTANCE.LiteRtDestroyModel(this)
    }

    companion object {
        fun create(filePath: String): LiteRtModel {
            val ref = PointerByReference()
            LiteRtLibrary.INSTANCE.LiteRtCreateModelFromFile(fileName = filePath, model = ref)

            val model = LiteRtModel()
            model.pointer = ref.value
            return model
        }
    }
}