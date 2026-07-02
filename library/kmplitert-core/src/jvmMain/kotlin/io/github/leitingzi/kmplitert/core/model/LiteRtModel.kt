package io.github.leitingzi.kmplitert.core.model

import com.sun.jna.PointerType
import com.sun.jna.ptr.PointerByReference
import io.github.leitingzi.kmplitert.core.LiteRtLibrary

class LiteRtModel: PointerType() {
    fun destroy() {
        LiteRtLibrary.INSTANCE.LiteRtDestroyModel(this)
    }

    fun getSignature(index: Long): LiteRtSignature {
        val ref = PointerByReference()
        val status = LiteRtLibrary.INSTANCE.LiteRtGetModelSignature(this, index, ref)
        check(status == 0) {
            "Failed to get model signature: $status"
        }
        val signature = LiteRtSignature()
        signature.pointer = ref.value
        return signature
    }

    companion object {
        fun create(filePath: String): LiteRtModel {
            val ref = PointerByReference()
            val status = LiteRtLibrary.INSTANCE.LiteRtCreateModelFromFile(
                fileName = filePath,
                model = ref
            )

            check(status == 0) {
                "Failed to create model from file: $status"
            }

            val model = LiteRtModel()
            model.pointer = ref.value
            return model
        }
    }
}

