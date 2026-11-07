package io.github.kmplitert.core.model

import com.sun.jna.Pointer
import com.sun.jna.PointerType
import com.sun.jna.ptr.PointerByReference
import io.github.kmplitert.core.LiteRtLibrary
import io.github.kmplitert.core.LiteRtStatus
import io.github.kmplitert.core.SizeTByReference

internal enum class LiteRtTensorBufferLockMode(val value: Int) {
    Read(0),
    Write(1),
    ReadWrite(2)
}

class LiteRtTensorBuffer : PointerType() {
    fun clear(): LiteRtStatus {
        val status = LiteRtLibrary.INSTANCE.LiteRtClearTensorBuffer(this)
        check(status == 0) {
            "Failed to clear tensor buffer: $status"
        }
        return 0
    }

    fun destroy() {
        LiteRtLibrary.INSTANCE.LiteRtDestroyTensorBuffer(this)
    }

    fun lock(lockMode: Int = LiteRtTensorBufferLockMode.ReadWrite.value): Pointer {
        require(LiteRtTensorBufferLockMode.entries.any { it.value == lockMode }) {
            "Invalid tensor buffer lock mode: $lockMode"
        }
        val hostMemAddrRef = PointerByReference()
        val status = LiteRtLibrary.INSTANCE.LiteRtLockTensorBuffer(
            tensor_buffer = this,
            host_mem_addr = hostMemAddrRef,
            lock_mode = lockMode
        )
        check(status == 0) {
            "Failed to lock tensor buffer: $status"
        }
        return hostMemAddrRef.value
    }

    internal fun lock(lockMode: LiteRtTensorBufferLockMode): Pointer = lock(lockMode.value)

    fun getPackedSize(): Long {
        val sizeRef = SizeTByReference()
        val status = LiteRtLibrary.INSTANCE.LiteRtGetTensorBufferPackedSize(
            tensor_buffer = this,
            size = sizeRef
        )
        check(status == 0) {
            "Failed to get tensor buffer packed size: $status"
        }
        return sizeRef.value
    }

    fun unlock() {
        LiteRtLibrary.INSTANCE.LiteRtUnlockTensorBuffer(this)
    }
}
