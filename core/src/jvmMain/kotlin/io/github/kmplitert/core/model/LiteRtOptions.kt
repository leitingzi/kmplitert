package io.github.kmplitert.core.model

import com.sun.jna.Pointer
import com.sun.jna.PointerType
import com.sun.jna.ptr.PointerByReference
import io.github.kmplitert.core.LiteRtLibrary

class LiteRtOptions : PointerType() {
    private val retainedGpuOptions = mutableListOf<LiteRtGpuOptions>()
    private var destroyed = false

    fun destroy() {
        if (!destroyed) {
            destroyed = true
            try {
                LiteRtLibrary.INSTANCE.LiteRtDestroyOptions(this)
            } finally {
                retainedGpuOptions.forEach(LiteRtGpuOptions::destroy)
                retainedGpuOptions.clear()
                pointer = null
            }
        }
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
        var opaqueOptions: Pointer? = null
        try {
            val createdOpaqueOptions = gpuOptions.createOpaqueOptions()
            opaqueOptions = createdOpaqueOptions
            addOpaqueOptions(createdOpaqueOptions)
        } catch (failure: Throwable) {
            opaqueOptions?.let(LiteRtLibrary.INSTANCE::LiteRtDestroyOpaqueOptions)
            gpuOptions.destroy()
            throw failure
        }
        retainedGpuOptions.add(gpuOptions)
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
