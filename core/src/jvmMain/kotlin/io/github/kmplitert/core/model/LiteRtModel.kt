package io.github.kmplitert.core.model

import com.sun.jna.PointerType
import com.sun.jna.ptr.PointerByReference
import io.github.kmplitert.core.LiteRtLibrary
import io.github.kmplitert.core.SizeTByReference
import io.github.kmplitert.core.asSizeT
import io.github.kmplitert.core.checkLiteRtStatus
import java.util.concurrent.atomic.AtomicBoolean

class LiteRtModel : PointerType() {
    private val destroyed = AtomicBoolean(false)

    fun destroy() {
        if (destroyed.compareAndSet(false, true)) {
            try {
                LiteRtLibrary.INSTANCE.LiteRtDestroyModel(this)
            } finally {
                pointer = null
            }
        }
    }

    fun getSignature(index: Long): LiteRtSignature {
        check(!destroyed.get()) { "Model is destroyed" }
        val ref = PointerByReference()
        val status = LiteRtLibrary.INSTANCE.LiteRtGetModelSignature(this, index.asSizeT(), ref)
        checkLiteRtStatus(status, "LiteRtGetModelSignature")
        val signature = LiteRtSignature()
        signature.pointer = ref.value
        return signature
    }

    fun getNumSignatures(): Long {
        check(!destroyed.get()) { "Model is destroyed" }
        val count = SizeTByReference()
        val status = LiteRtLibrary.INSTANCE.LiteRtGetNumModelSignatures(this, count)
        checkLiteRtStatus(status, "LiteRtGetNumModelSignatures")
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

            checkLiteRtStatus(status, "LiteRtCreateModelFromFile")

            val model = LiteRtModel()
            model.pointer = requireNotNull(ref.value) {
                "LiteRtCreateModelFromFile returned a null handle"
            }
            return model
        }
    }
}
