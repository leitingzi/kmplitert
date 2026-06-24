package com.leitz.kmplitert.model

import com.leitz.kmplitert.newApi.LiteRtLibrary
import com.sun.jna.PointerType
import com.sun.jna.ptr.PointerByReference

class LiteRtOptions : PointerType() {
    fun destroy() {
        LiteRtLibrary.INSTANCE.LiteRtDestroyOptions(this)
    }

    fun setAccelerators(liteRtHwAcceleratorSet: LiteRtHwAcceleratorSet) {
        LiteRtLibrary.INSTANCE.LiteRtSetOptionsHardwareAccelerators(
            options = this,
            hardware_accelerators = liteRtHwAcceleratorSet.value
        )
    }

    companion object {
        fun create(): LiteRtOptions {
            val ref = PointerByReference()
            LiteRtLibrary.INSTANCE.LiteRtCreateOptions(ref)

            val options = LiteRtOptions()
            options.pointer = ref.value
            return options
        }
    }
}