package io.github.kmplitert.core.model

import com.sun.jna.Pointer
import com.sun.jna.PointerType
import com.sun.jna.ptr.PointerByReference
import io.github.kmplitert.core.LiteRtLibrary
import io.github.kmplitert.core.checkLiteRtStatus
import java.util.concurrent.atomic.AtomicBoolean

class LiteRtOptions : PointerType() {
    private val retainedGpuOptions = mutableListOf<LiteRtGpuOptions>()
    private val destroyed = AtomicBoolean(false)

    fun destroy() {
        if (destroyed.compareAndSet(false, true)) {
            var failure: Throwable? = null
            try {
                LiteRtLibrary.INSTANCE.LiteRtDestroyOptions(this)
            } catch (destroyFailure: Throwable) {
                failure = destroyFailure
            }
            retainedGpuOptions.forEach { options ->
                try {
                    options.destroy()
                } catch (cleanupFailure: Throwable) {
                    if (failure == null) {
                        failure = cleanupFailure
                    } else {
                        failure.addSuppressed(cleanupFailure)
                    }
                }
            }
            retainedGpuOptions.clear()
            pointer = null
            failure?.let { throw it }
        }
    }

    fun setAccelerators(liteRtHwAcceleratorSet: LiteRtHwAcceleratorSet) {
        check(!destroyed.get()) { "Options are destroyed" }
        val status = LiteRtLibrary.INSTANCE.LiteRtSetOptionsHardwareAccelerators(
            options = this,
            hardware_accelerators = liteRtHwAcceleratorSet.value
        )
        checkLiteRtStatus(status, "LiteRtSetOptionsHardwareAccelerators")
    }

    fun addOpaqueOptions(opaqueOptions: Pointer) {
        check(!destroyed.get()) { "Options are destroyed" }
        val status = LiteRtLibrary.INSTANCE.LiteRtAddOpaqueOptions(
            options = this,
            opaque_options = opaqueOptions
        )
        checkLiteRtStatus(status, "LiteRtAddOpaqueOptions")
    }

    fun addGpuOptions(gpuOptions: LiteRtGpuOptions) {
        check(!destroyed.get()) { "Options are destroyed" }
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

            checkLiteRtStatus(status, "LiteRtCreateOptions")

            val options = LiteRtOptions()
            options.pointer = requireNotNull(ref.value) {
                "LiteRtCreateOptions returned a null handle"
            }
            return options
        }
    }
}
