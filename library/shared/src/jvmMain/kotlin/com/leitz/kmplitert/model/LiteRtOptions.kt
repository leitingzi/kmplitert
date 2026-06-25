package com.leitz.kmplitert.model

import com.leitz.kmplitert.newApi.LiteRtLibrary
import com.sun.jna.PointerType
import com.sun.jna.ptr.PointerByReference

class LiteRtOptions : PointerType() {
    fun destroy() {
        LiteRtLibrary.INSTANCE.LiteRtDestroyOptions(this)
    }

    fun setAccelerators(liteRtHwAcceleratorSet: LiteRtHwAcceleratorSet) {
        val status = LiteRtLibrary.INSTANCE.LiteRtSetOptionsHardwareAccelerators(
            options = this,
            hardware_accelerators = liteRtHwAcceleratorSet.value
        )
        check(status == 0) {
            "Failed to set hardware accelerators: $status"
        }
    }

    companion object {
        fun create(): LiteRtOptions {
            val ref = PointerByReference()
            val status = LiteRtLibrary.INSTANCE.LiteRtCreateOptions(ref)

            check(status == 0) {
                "Failed to create options: $status"
            }

            val options = LiteRtOptions()
            options.pointer = ref.value
            return options
        }
    }
}