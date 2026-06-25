package com.leitz.kmplitert.model

import com.leitz.kmplitert.newApi.LiteRtLibrary
import com.sun.jna.PointerType
import com.sun.jna.ptr.LongByReference

class LiteRtSignature : PointerType() {
    fun getNumInputs(): Long {
        val count = LongByReference()
        val status = LiteRtLibrary.INSTANCE.LiteRtGetNumSignatureInputs(this.pointer, count)
        check(status == 0) {
            "Failed to get num signature inputs: $status"
        }
        return count.value
    }

    fun getNumOutputs(): Long {
        val count = LongByReference()
        val status = LiteRtLibrary.INSTANCE.LiteRtGetNumSignatureOutputs(this.pointer, count)
        check(status == 0) {
            "Failed to get num signature outputs: $status"
        }
        return count.value
    }
}
