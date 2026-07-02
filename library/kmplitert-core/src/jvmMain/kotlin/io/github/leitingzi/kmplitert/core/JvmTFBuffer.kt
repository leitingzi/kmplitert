package io.github.leitingzi.kmplitert.core

import io.github.leitingzi.kmplitert.core.model.LiteRtTensorBuffer

class JvmTFBuffer (val buffer: LiteRtTensorBuffer): TFBuffer {
    override fun writeInt(data: IntArray) {
        val inputAddr = buffer.lock(1) // kLiteRtTensorBufferLockModeWrite
        inputAddr.write(0, data, 0, data.size)
        buffer.unlock()
    }

    override fun writeFloat(data: FloatArray) {
        val inputAddr = buffer.lock(1) // kLiteRtTensorBufferLockModeWrite
        inputAddr.write(0, data, 0, data.size)
        buffer.unlock()
    }

    override fun writeInt8(data: ByteArray) {
        val inputAddr = buffer.lock(1) // kLiteRtTensorBufferLockModeWrite
        inputAddr.write(0, data, 0, data.size)
        buffer.unlock()
    }

    override fun writeBoolean(data: BooleanArray) {
        val inputAddr = buffer.lock(1) // kLiteRtTensorBufferLockModeWrite
        val bytes = ByteArray(data.size) { if (data[it]) 1 else 0 }
        inputAddr.write(0, bytes, 0, bytes.size)
        buffer.unlock()
    }

    override fun writeLong(data: LongArray) {
        val inputAddr = buffer.lock(1) // kLiteRtTensorBufferLockModeWrite
        inputAddr.write(0, data, 0, data.size)
        buffer.unlock()
    }

    override suspend fun readInt(): IntArray {
        val size = buffer.getPackedSize()
        val numElements = (size / 4).toInt()
        val outputAddr = buffer.lock(0) // kLiteRtTensorBufferLockModeRead
        val results = IntArray(numElements)
        outputAddr.read(0, results, 0, numElements)
        buffer.unlock()
        return results
    }

    override suspend fun readFloat(): FloatArray {
        val size = buffer.getPackedSize()
        val numElements = (size / 4).toInt()
        val outputAddr = buffer.lock(0) // kLiteRtTensorBufferLockModeRead
        val results = FloatArray(numElements)
        outputAddr.read(0, results, 0, numElements)
        buffer.unlock()
        return results
    }

    override suspend fun readInt8(): ByteArray {
        val size = buffer.getPackedSize()
        val numElements = size.toInt()
        val outputAddr = buffer.lock(0) // kLiteRtTensorBufferLockModeRead
        val results = ByteArray(numElements)
        outputAddr.read(0, results, 0, numElements)
        buffer.unlock()
        return results
    }

    override suspend fun readBoolean(): BooleanArray {
        val size = buffer.getPackedSize()
        val numElements = size.toInt()
        val outputAddr = buffer.lock(0) // kLiteRtTensorBufferLockModeRead
        val bytes = ByteArray(numElements)
        outputAddr.read(0, bytes, 0, numElements)
        buffer.unlock()
        return BooleanArray(numElements) { bytes[it] != 0.toByte() }
    }

    override suspend fun readLong(): LongArray {
        val size = buffer.getPackedSize()
        val numElements = (size / 8).toInt()
        val outputAddr = buffer.lock(0) // kLiteRtTensorBufferLockModeRead
        val results = LongArray(numElements)
        outputAddr.read(0, results, 0, numElements)
        buffer.unlock()
        return results
    }
}

