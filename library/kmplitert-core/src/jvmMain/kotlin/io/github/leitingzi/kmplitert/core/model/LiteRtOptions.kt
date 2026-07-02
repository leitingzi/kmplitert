package io.github.leitingzi.kmplitert.core.model

import com.sun.jna.Pointer
import com.sun.jna.PointerType
import com.sun.jna.ptr.PointerByReference
import io.github.leitingzi.kmplitert.core.LiteRtLibrary

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

    fun addOpaqueOptions(opaqueOptions: Pointer) {
        val status = LiteRtLibrary.INSTANCE.LiteRtAddOpaqueOptions(
            options = this,
            opaque_options = opaqueOptions
        )
        check(status == 0) {
            "Failed to add opaque options: $status"
        }
    }

    fun addGpuOptions(gpuOptions: LiteRtGpuOptions) {
        val opaqueOptions = gpuOptions.createOpaqueOptions()
        addOpaqueOptions(opaqueOptions)
    }

    companion object {
        fun create(): LiteRtOptions {
            val ref = PointerByReference()
            val status = LiteRtLibrary.INSTANCE.LiteRtCreateOptions(options = ref)

            check(status == 0) {
                "Failed to create options: $status"
            }

            val options = LiteRtOptions()
            options.pointer = ref.value
            return options
        }
    }
}

