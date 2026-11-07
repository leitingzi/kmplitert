package io.github.kmplitert.core

import io.github.kmplitert.core.model.LiteRtTensorBuffer
import io.github.kmplitert.core.model.LiteRtTensorBufferLockMode
import java.util.concurrent.atomic.AtomicBoolean

class JvmTFBuffer(
    val buffer: LiteRtTensorBuffer
) : TFBuffer {
    private val closed = AtomicBoolean(false)

    override fun writeInt(data: IntArray) {
        withLock(LiteRtTensorBufferLockMode.Write) { it.write(0, data, 0, data.size) }
    }

    override fun writeFloat(data: FloatArray) {
        withLock(LiteRtTensorBufferLockMode.Write) { it.write(0, data, 0, data.size) }
    }

    override fun writeInt8(data: ByteArray) {
        withLock(LiteRtTensorBufferLockMode.Write) { it.write(0, data, 0, data.size) }
    }

    override fun writeBoolean(data: BooleanArray) {
        val bytes = ByteArray(data.size) { if (data[it]) 1 else 0 }
        withLock(LiteRtTensorBufferLockMode.Write) { it.write(0, bytes, 0, bytes.size) }
    }

    override fun writeLong(data: LongArray) {
        withLock(LiteRtTensorBufferLockMode.Write) { it.write(0, data, 0, data.size) }
    }

    override suspend fun readInt(): IntArray {
        ensureOpen()
        val size = buffer.getPackedSize()
        val numElements = (size / 4).toInt()
        return withLock(LiteRtTensorBufferLockMode.Read) { outputAddr ->
            IntArray(numElements).also { outputAddr.read(0, it, 0, numElements) }
        }
    }

    override suspend fun readFloat(): FloatArray {
        ensureOpen()
        val size = buffer.getPackedSize()
        val numElements = (size / 4).toInt()
        return withLock(LiteRtTensorBufferLockMode.Read) { outputAddr ->
            FloatArray(numElements).also { outputAddr.read(0, it, 0, numElements) }
        }
    }

    override suspend fun readInt8(): ByteArray {
        ensureOpen()
        val size = buffer.getPackedSize()
        val numElements = size.toInt()
        return withLock(LiteRtTensorBufferLockMode.Read) { outputAddr ->
            ByteArray(numElements).also { outputAddr.read(0, it, 0, numElements) }
        }
    }

    override suspend fun readBoolean(): BooleanArray {
        ensureOpen()
        val size = buffer.getPackedSize()
        val numElements = size.toInt()
        return withLock(LiteRtTensorBufferLockMode.Read) { outputAddr ->
            val bytes = ByteArray(numElements)
            outputAddr.read(0, bytes, 0, numElements)
            BooleanArray(numElements) { bytes[it] != 0.toByte() }
        }
    }

    override suspend fun readLong(): LongArray {
        ensureOpen()
        val size = buffer.getPackedSize()
        val numElements = (size / 8).toInt()
        return withLock(LiteRtTensorBufferLockMode.Read) { outputAddr ->
            LongArray(numElements).also { outputAddr.read(0, it, 0, numElements) }
        }
    }

    private inline fun <T> withLock(lockMode: LiteRtTensorBufferLockMode, block: (com.sun.jna.Pointer) -> T): T {
        ensureOpen()
        val address = buffer.lock(lockMode)
        try {
            return block(address)
        } finally {
            buffer.unlock()
        }
    }

    override fun close() {
        if (closed.compareAndSet(false, true)) {
            buffer.destroy()
        }
    }

    internal fun ensureOpen() {
        check(!closed.get()) { "Tensor buffer is closed" }
    }
}
