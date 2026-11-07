package io.github.kmplitert.core.model

import com.sun.jna.PointerType
import com.sun.jna.ptr.PointerByReference
import io.github.kmplitert.core.LiteRtLibrary
import io.github.kmplitert.core.SizeTByReference
import io.github.kmplitert.core.asSizeT
import io.github.kmplitert.core.checkLiteRtStatus

class LiteRtSignature : PointerType() {
    fun getNumInputs(): Long {
        val count = SizeTByReference()
        val status = LiteRtLibrary.INSTANCE.LiteRtGetNumSignatureInputs(this, count)
        checkLiteRtStatus(status, "LiteRtGetNumSignatureInputs")
        return count.value
    }

    fun getNumOutputs(): Long {
        val count = SizeTByReference()
        val status = LiteRtLibrary.INSTANCE.LiteRtGetNumSignatureOutputs(this, count)
        checkLiteRtStatus(status, "LiteRtGetNumSignatureOutputs")
        return count.value
    }

    fun getInputTensor(name: String): LiteRtTensor {
        val ref = PointerByReference()
        val status = LiteRtLibrary.INSTANCE.LiteRtGetSignatureInputTensor(this, name, ref)
        checkLiteRtStatus(status, "LiteRtGetSignatureInputTensor")
        val tensor = LiteRtTensor()
        tensor.pointer = ref.value
        return tensor
    }

    fun getOutputTensor(name: String): LiteRtTensor {
        val ref = PointerByReference()
        val status = LiteRtLibrary.INSTANCE.LiteRtGetSignatureOutputTensor(this, name, ref)
        checkLiteRtStatus(status, "LiteRtGetSignatureOutputTensor")
        val tensor = LiteRtTensor()
        tensor.pointer = ref.value
        return tensor
    }

    fun getInputTensor(index: Long): LiteRtTensor {
        val ref = PointerByReference()
        val status = LiteRtLibrary.INSTANCE.LiteRtGetSignatureInputTensorByIndex(this, index.asSizeT(), ref)
        checkLiteRtStatus(status, "LiteRtGetSignatureInputTensorByIndex")
        val tensor = LiteRtTensor()
        tensor.pointer = ref.value
        return tensor
    }

    fun getOutputTensor(index: Long): LiteRtTensor {
        val ref = PointerByReference()
        val status = LiteRtLibrary.INSTANCE.LiteRtGetSignatureOutputTensorByIndex(this, index.asSizeT(), ref)
        checkLiteRtStatus(status, "LiteRtGetSignatureOutputTensorByIndex")
        val tensor = LiteRtTensor()
        tensor.pointer = ref.value
        return tensor
    }

    fun getInputName(index: Long): String {
        val ref = PointerByReference()
        val status = LiteRtLibrary.INSTANCE.LiteRtGetSignatureInputName(this, index.asSizeT(), ref)
        checkLiteRtStatus(status, "LiteRtGetSignatureInputName")
        return ref.value.getString(0, "UTF-8")
    }

    fun getOutputName(index: Long): String {
        val ref = PointerByReference()
        val status = LiteRtLibrary.INSTANCE.LiteRtGetSignatureOutputName(this, index.asSizeT(), ref)
        checkLiteRtStatus(status, "LiteRtGetSignatureOutputName")
        return ref.value.getString(0, "UTF-8")
    }
}
