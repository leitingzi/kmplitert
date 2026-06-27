package io.github.leitingzi.kmplitert.core.model

import com.leitz.kmplitert.LiteRtLibrary
import com.leitz.kmplitert.LiteRtStatus
import com.sun.jna.Pointer
import com.sun.jna.PointerType
import com.sun.jna.ptr.PointerByReference

class LiteRtTensorBuffer: PointerType() {
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

    fun lock(): Pointer {
        val hostMemAddrRef = PointerByReference()
        val status = LiteRtLibrary.INSTANCE.LiteRtLockTensorBuffer(
            tensor_buffer = this,
            host_mem_addr = hostMemAddrRef,
            lock_mode = 1
        )
        check(status == 0) {
            "Failed to lock tensor buffer: $status"
        }
        return hostMemAddrRef.value
    }

    fun getPackedSize(): Long {
        val sizeRef = com.sun.jna.ptr.LongByReference()
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