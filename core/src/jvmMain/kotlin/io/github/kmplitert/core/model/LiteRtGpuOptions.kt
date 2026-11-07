package io.github.kmplitert.core.model

import com.sun.jna.Pointer
import com.sun.jna.PointerType
import com.sun.jna.ptr.PointerByReference
import io.github.kmplitert.core.LiteRtLibrary
import io.github.kmplitert.core.checkLiteRtStatus
import java.util.concurrent.atomic.AtomicBoolean

class LiteRtGpuOptions : PointerType() {
    private val destroyed = AtomicBoolean(false)

    init {
        val ref = PointerByReference()
        val status = LiteRtLibrary.INSTANCE.LrtCreateGpuOptions(ref)
        checkLiteRtStatus(status, "LrtCreateGpuOptions")
        pointer = requireNotNull(ref.value) { "GPU options pointer is null" }
    }

    fun setBackend(backend: LiteRtGpuBackend) {
        check(!destroyed.get()) { "GPU options are destroyed" }
        val status = LiteRtLibrary.INSTANCE.LrtSetGpuOptionsGpuBackend(pointer, backend.value)
        checkLiteRtStatus(status, "LrtSetGpuOptionsGpuBackend")
    }

    fun createOpaqueOptions(): Pointer {
        check(!destroyed.get()) { "GPU options are destroyed" }
        val identifier = PointerByReference()
        val payload = PointerByReference()
        val destructor = PointerByReference()
        val dataStatus = LiteRtLibrary.INSTANCE.LrtGetOpaqueGpuOptionsData(
            options = pointer,
            identifier = identifier,
            payload = payload,
            payload_destructor = destructor
        )
        checkLiteRtStatus(dataStatus, "LrtGetOpaqueGpuOptionsData")

        val identifierPointer = requireNotNull(identifier.value) { "GPU options identifier is null" }
        val payloadPointer = requireNotNull(payload.value) { "GPU options payload is null" }
        val destructorPointer = requireNotNull(destructor.value) { "GPU options destructor is null" }
        val opaqueOptions = PointerByReference()
        val status = LiteRtLibrary.INSTANCE.LiteRtCreateOpaqueOptions(
            identifier = identifierPointer.getString(0, "UTF-8"),
            data = payloadPointer,
            destructor = destructorPointer,
            opaque_options = opaqueOptions
        )
        checkLiteRtStatus(status, "LiteRtCreateOpaqueOptions")
        return requireNotNull(opaqueOptions.value) { "Opaque GPU options pointer is null" }
    }

    fun destroy() {
        if (destroyed.compareAndSet(false, true)) {
            try {
                LiteRtLibrary.INSTANCE.LrtDestroyGpuOptions(pointer)
            } finally {
                pointer = null
            }
        }
    }

    companion object {
        fun create(): LiteRtGpuOptions {
            return LiteRtGpuOptions()
        }

        fun destroyOpaqueOptions(opaqueOptions: Pointer) {
            LiteRtLibrary.INSTANCE.LiteRtDestroyOpaqueOptions(opaqueOptions)
        }
    }
}

enum class LiteRtGpuBackend(val value: Int) {
    AUTOMATIC(0),
    OPENCL(1),
    WEBGPU(2),
    OPENGL(3)
}
