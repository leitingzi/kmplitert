package com.leitz.kmplitert.model

import com.leitz.kmplitert.LITERT_TENSOR_BUFFER_LOCK_MODE_WRITE
import com.leitz.kmplitert.LiteRtStatus
import com.leitz.kmplitert.newApi.LiteRtLibrary
import com.sun.jna.Pointer
import com.sun.jna.PointerType
import com.sun.jna.ptr.PointerByReference

class LiteRtTensorBuffer: PointerType() {
    fun clear(): LiteRtStatus {
        return LiteRtLibrary.INSTANCE.LiteRtClearTensorBuffer(this)
    }

    fun destroy() {
        LiteRtLibrary.INSTANCE.LiteRtDestroyTensorBuffer(this)
    }

    fun lock(): Pointer {
        val hostMemAddrRef = PointerByReference()
        val status = LiteRtLibrary.INSTANCE.LiteRtLockTensorBuffer(
            tensor_buffer = this,
            host_mem_addr = hostMemAddrRef,
            lock_mode = LITERT_TENSOR_BUFFER_LOCK_MODE_WRITE
        )
        check(status == 0) { "Failed to lock tensor buffer: $status" }
        return hostMemAddrRef.value
    }

    fun unlock() {
        LiteRtLibrary.INSTANCE.LiteRtUnlockTensorBuffer(this)
    }
}