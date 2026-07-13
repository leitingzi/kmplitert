@file:OptIn(ExperimentalForeignApi::class)

package io.github.leitingzi.kmplitert.core

import kotlinx.cinterop.*
import kotlinx.cinterop.get
import litert.*

class NativeTFBuffer(
    val buffer: LiteRtTensorBuffer
) : TFBuffer {
    
    private fun <T> withLockedBuffer(mode: LiteRtTensorBufferLockMode, block: (COpaquePointer) -> T): T {
        return memScoped {
            val addrRef = alloc<COpaquePointerVar>()
            val status = LiteRtLockTensorBuffer(buffer, addrRef.ptr, mode)
            check(status == kLiteRtStatusOk) { "Failed to lock buffer: $status" }
            try {
                block(addrRef.value!!)
            } finally {
                LiteRtUnlockTensorBuffer(buffer)
            }
        }
    }

    override fun writeInt(data: IntArray) {
        withLockedBuffer(kLiteRtTensorBufferLockModeWrite) { addr ->
            data.usePinned { pinned ->
                platform.posix.memcpy(addr, pinned.addressOf(0), (data.size * 4).convert())
            }
        }
    }

    override fun writeFloat(data: FloatArray) {
        withLockedBuffer(kLiteRtTensorBufferLockModeWrite) { addr ->
            data.usePinned { pinned ->
                platform.posix.memcpy(addr, pinned.addressOf(0), (data.size * 4).convert())
            }
        }
    }

    override fun writeInt8(data: ByteArray) {
        withLockedBuffer(kLiteRtTensorBufferLockModeWrite) { addr ->
            data.usePinned { pinned ->
                platform.posix.memcpy(addr, pinned.addressOf(0), data.size.convert())
            }
        }
    }

    override fun writeBoolean(data: BooleanArray) {
        withLockedBuffer(kLiteRtTensorBufferLockModeWrite) { addr ->
            val hostAddr = addr.reinterpret<ByteVar>()
            for (i in data.indices) {
                hostAddr[i] = if (data[i]) 1.toByte() else 0.toByte()
            }
        }
    }

    override fun writeLong(data: LongArray) {
        withLockedBuffer(kLiteRtTensorBufferLockModeWrite) { addr ->
            data.usePinned { pinned ->
                platform.posix.memcpy(addr, pinned.addressOf(0), (data.size * 8).convert())
            }
        }
    }

    private fun getBufferSize(): Long {
        return memScoped {
            val sizeRef = alloc<ULongVar>()
            val status = LiteRtGetTensorBufferPackedSize(buffer, sizeRef.ptr)
            check(status == kLiteRtStatusOk) { "Failed to get buffer size: $status" }
            sizeRef.value.toLong()
        }
    }

    override suspend fun readInt(): IntArray {
        val size = (getBufferSize() / 4).toInt()
        val data = IntArray(size)
        withLockedBuffer(kLiteRtTensorBufferLockModeRead) { addr ->
            data.usePinned { pinned ->
                platform.posix.memcpy(pinned.addressOf(0), addr, (size * 4).convert())
            }
        }
        return data
    }

    override suspend fun readFloat(): FloatArray {
        val size = (getBufferSize() / 4).toInt()
        val data = FloatArray(size)
        withLockedBuffer(kLiteRtTensorBufferLockModeRead) { addr ->
            data.usePinned { pinned ->
                platform.posix.memcpy(pinned.addressOf(0), addr, (size * 4).convert())
            }
        }
        return data
    }

    override suspend fun readInt8(): ByteArray {
        val size = getBufferSize().toInt()
        val data = ByteArray(size)
        withLockedBuffer(kLiteRtTensorBufferLockModeRead) { addr ->
            data.usePinned { pinned ->
                platform.posix.memcpy(pinned.addressOf(0), addr, size.convert())
            }
        }
        return data
    }

    override suspend fun readBoolean(): BooleanArray {
        val size = getBufferSize().toInt()
        val data = BooleanArray(size)
        withLockedBuffer(kLiteRtTensorBufferLockModeRead) { addr ->
            val hostAddr = addr.reinterpret<ByteVar>()
            for (i in 0 until size) { data[i] = hostAddr[i] != 0.toByte() }
        }
        return data
    }

    override suspend fun readLong(): LongArray {
        val size = (getBufferSize() / 8).toInt()
        val data = LongArray(size)
        withLockedBuffer(kLiteRtTensorBufferLockModeRead) { addr ->
            data.usePinned { pinned ->
                platform.posix.memcpy(pinned.addressOf(0), addr, (size * 8).convert())
            }
        }
        return data
    }
}


