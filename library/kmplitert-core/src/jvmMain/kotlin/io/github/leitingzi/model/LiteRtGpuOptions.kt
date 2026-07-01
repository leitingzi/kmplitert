package io.github.leitingzi.model

import com.sun.jna.Memory
import com.sun.jna.Pointer
import com.sun.jna.ptr.PointerByReference
import io.github.leitingzi.LiteRtLibrary

class LiteRtGpuOptions {
    private var backend: LiteRtGpuBackend? = null

    fun setBackend(backend: LiteRtGpuBackend) {
        this.backend = backend
    }

    fun createOpaqueOptions(): Pointer {
        val toml = buildString {
            backend?.let {
                append("backend = ${it.value}\n")
            }
        }

        val tomlBytes = toml.toByteArray(Charsets.UTF_8)
        val tomlPtr = Memory((tomlBytes.size + 1).toLong())
        tomlPtr.write(0, tomlBytes, 0, tomlBytes.size)
        tomlPtr.setByte(tomlBytes.size.toLong(), 0)

        val ref = PointerByReference()
        val status = LiteRtLibrary.INSTANCE.LiteRtCreateOpaqueOptions(
            identifier = "gpu_options",
            data = tomlPtr,
            size = (tomlBytes.size + 1).toLong(),
            opaque_options = ref
        )
        check(status == 0) {
            "Failed to create opaque GPU options: $status"
        }

        return ref.value
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
