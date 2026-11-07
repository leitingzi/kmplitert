package io.github.kmplitert.core.model

import com.sun.jna.Pointer
import com.sun.jna.PointerType
import com.sun.jna.ptr.PointerByReference
import io.github.kmplitert.core.LiteRtLibrary
import io.github.kmplitert.core.LiteRtStatus
import io.github.kmplitert.core.SizeTByReference
import io.github.kmplitert.core.checkLiteRtStatus
import java.util.concurrent.atomic.AtomicBoolean

internal enum class LiteRtTensorBufferLockMode(val value: Int) {
    Read(0),
    Write(1),
    ReadWrite(2)
}

class LiteRtTensorBuffer : PointerType() {
    private val destroyed = AtomicBoolean(false)

    fun clear(): LiteRtStatus {
        check(!destroyed.get()) { "Tensor buffer is destroyed" }
        val status = LiteRtLibrary.INSTANCE.LiteRtClearTensorBuffer(this)
        checkLiteRtStatus(status, "LiteRtClearTensorBuffer")
        return 0
    }

    fun destroy() {
        if (destroyed.compareAndSet(false, true)) {
            try {
                LiteRtLibrary.INSTANCE.LiteRtDestroyTensorBuffer(this)
            } finally {
                pointer = null
            }
        }
    }

    fun lock(lockMode: Int = LiteRtTensorBufferLockMode.ReadWrite.value): Pointer {
        check(!destroyed.get()) { "Tensor buffer is destroyed" }
        require(LiteRtTensorBufferLockMode.entries.any { it.value == lockMode }) {
            "Invalid tensor buffer lock mode: $lockMode"
        }
        val hostMemAddrRef = PointerByReference()
        val status = LiteRtLibrary.INSTANCE.LiteRtLockTensorBuffer(
            tensor_buffer = this,
            host_mem_addr = hostMemAddrRef,
            lock_mode = lockMode
        )
        checkLiteRtStatus(status, "LiteRtLockTensorBuffer")
        return hostMemAddrRef.value
    }

    internal fun lock(lockMode: LiteRtTensorBufferLockMode): Pointer = lock(lockMode.value)

    fun getPackedSize(): Long {
        check(!destroyed.get()) { "Tensor buffer is destroyed" }
        val sizeRef = SizeTByReference()
        val status = LiteRtLibrary.INSTANCE.LiteRtGetTensorBufferPackedSize(
            tensor_buffer = this,
            size = sizeRef
        )
        checkLiteRtStatus(status, "LiteRtGetTensorBufferPackedSize")
        return sizeRef.value
    }

    fun unlock() {
        LiteRtLibrary.INSTANCE.LiteRtUnlockTensorBuffer(this)
    }
}
