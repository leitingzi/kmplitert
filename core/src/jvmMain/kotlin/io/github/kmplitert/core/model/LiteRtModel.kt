package io.github.kmplitert.core.model

import com.sun.jna.PointerType
import com.sun.jna.ptr.PointerByReference
import io.github.kmplitert.core.LiteRtLibrary
import io.github.kmplitert.core.SizeTByReference
import io.github.kmplitert.core.asSizeT

class LiteRtModel : PointerType() {
    fun destroy() {
        LiteRtLibrary.INSTANCE.LiteRtDestroyModel(this)
    }

    fun getSignature(index: Long): LiteRtSignature {
        val ref = PointerByReference()
        val status = LiteRtLibrary.INSTANCE.LiteRtGetModelSignature(this, index.asSizeT(), ref)
        check(status == 0) {
            "Failed to get model signature: $status"
        }
        val signature = LiteRtSignature()
        signature.pointer = ref.value
        return signature
    }

    fun getNumSignatures(): Long {
        val count = SizeTByReference()
        val status = LiteRtLibrary.INSTANCE.LiteRtGetNumModelSignatures(this, count)
        check(status == 0) {
            "Failed to get num signatures: $status"
        }
        return count.value
    }

    companion object {
        fun create(environment: LiteRtEnvironment, filePath: String): LiteRtModel {
            val ref = PointerByReference()
            val status = LiteRtLibrary.INSTANCE.LiteRtCreateModelFromFile(
                environment = environment,
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
