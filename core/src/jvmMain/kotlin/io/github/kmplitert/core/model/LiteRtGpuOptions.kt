package io.github.kmplitert.core.model

import com.sun.jna.Pointer
import com.sun.jna.PointerType
import com.sun.jna.ptr.PointerByReference
import io.github.kmplitert.core.LiteRtLibrary

class LiteRtGpuOptions : PointerType() {
    init {
        val ref = PointerByReference()
        val status = LiteRtLibrary.INSTANCE.LrtCreateGpuOptions(ref)
        check(status == 0) { "Failed to create GPU options: $status" }
        pointer = requireNotNull(ref.value) { "GPU options pointer is null" }
    }

    fun setBackend(backend: LiteRtGpuBackend) {
        val status = LiteRtLibrary.INSTANCE.LrtSetGpuOptionsGpuBackend(pointer, backend.value)
        check(status == 0) { "Failed to set GPU backend: $status" }
    }

    fun createOpaqueOptions(): Pointer {
        val identifier = PointerByReference()
        val payload = PointerByReference()
        val destructor = PointerByReference()
        val dataStatus = LiteRtLibrary.INSTANCE.LrtGetOpaqueGpuOptionsData(
            options = pointer,
            identifier = identifier,
            payload = payload,
            payload_destructor = destructor
        )
        check(dataStatus == 0) { "Failed to get opaque GPU options data: $dataStatus" }

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
        check(status == 0) { "Failed to create opaque GPU options: $status" }
        return requireNotNull(opaqueOptions.value) { "Opaque GPU options pointer is null" }
    }

    fun destroy() {
        if (pointer != null) {
            LiteRtLibrary.INSTANCE.LrtDestroyGpuOptions(pointer)
            pointer = null
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
