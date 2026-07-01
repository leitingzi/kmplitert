package io.github.leitingzi.model

import com.sun.jna.PointerType
import com.sun.jna.ptr.LongByReference
import com.sun.jna.ptr.PointerByReference
import io.github.leitingzi.LiteRtLibrary

class LiteRtSignature : PointerType() {
    fun getNumInputs(): Long {
        val count = LongByReference()
        val status = LiteRtLibrary.INSTANCE.LiteRtGetNumSignatureInputs(this, count)
        check(status == 0) {
            "Failed to get num signature inputs: $status"
        }
        return count.value
    }

    fun getNumOutputs(): Long {
        val count = LongByReference()
        val status = LiteRtLibrary.INSTANCE.LiteRtGetNumSignatureOutputs(this, count)
        check(status == 0) {
            "Failed to get num signature outputs: $status"
        }
        return count.value
    }

    fun getInputTensor(index: Long): LiteRtTensor {
        val ref = PointerByReference()
        val status = LiteRtLibrary.INSTANCE.LiteRtGetSignatureInputTensorByIndex(this, index, ref)
        check(status == 0) {
            "Failed to get input tensor: $status"
        }
        val tensor = LiteRtTensor()
        tensor.pointer = ref.value
        return tensor
    }

    fun getOutputTensor(index: Long): LiteRtTensor {
        val ref = PointerByReference()
        val status = LiteRtLibrary.INSTANCE.LiteRtGetSignatureOutputTensorByIndex(this, index, ref)
        check(status == 0) {
            "Failed to get output tensor: $status"
        }
        val tensor = LiteRtTensor()
        tensor.pointer = ref.value
        return tensor
    }
}
