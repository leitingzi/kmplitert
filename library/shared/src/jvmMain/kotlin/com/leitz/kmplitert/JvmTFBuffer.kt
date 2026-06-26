package com.leitz.kmplitert

import com.leitz.kmplitert.model.LiteRtTensorBuffer

class JvmTFBuffer (val buffer: LiteRtTensorBuffer): TFBuffer {
    override fun writeInt(data: IntArray) {
        val inputAddr = buffer.lock()
        inputAddr.write(0, data, 0, data.size)
        buffer.unlock()
    }

    override fun writeFloat(data: FloatArray) {
        val inputAddr = buffer.lock()
        inputAddr.write(0, data, 0, data.size)
        buffer.unlock()
    }

    override fun writeInt8(data: ByteArray) {
        val inputAddr = buffer.lock()
        inputAddr.write(0, data, 0, data.size)
        buffer.unlock()
    }

    override fun writeBoolean(data: BooleanArray) {
        val inputAddr = buffer.lock()
        val bytes = ByteArray(data.size) { if (data[it]) 1 else 0 }
        inputAddr.write(0, bytes, 0, bytes.size)
        buffer.unlock()
    }

    override fun writeLong(data: LongArray) {
        val inputAddr = buffer.lock()
        inputAddr.write(0, data, 0, data.size)
        buffer.unlock()
    }

    override suspend fun readInt(): IntArray {
        val size = buffer.getPackedSize()
        val numElements = (size / 4).toInt()
        val outputAddr = buffer.lock()
        val results = IntArray(numElements)
        outputAddr.read(0, results, 0, numElements)
        buffer.unlock()
        return results
    }

    override suspend fun readFloat(): FloatArray {
        val size = buffer.getPackedSize()
        val numElements = (size / 4).toInt()
        val outputAddr = buffer.lock()
        val results = FloatArray(numElements)
        outputAddr.read(0, results, 0, numElements)
        buffer.unlock()
        return results
    }

    override suspend fun readInt8(): ByteArray {
        val size = buffer.getPackedSize()
        val numElements = size.toInt()
        val outputAddr = buffer.lock()
        val results = ByteArray(numElements)
        outputAddr.read(0, results, 0, numElements)
        buffer.unlock()
        return results
    }

    override suspend fun readBoolean(): BooleanArray {
        val size = buffer.getPackedSize()
        val numElements = size.toInt()
        val outputAddr = buffer.lock()
        val bytes = ByteArray(numElements)
        outputAddr.read(0, bytes, 0, numElements)
        buffer.unlock()
        return BooleanArray(numElements) { bytes[it] != 0.toByte() }
    }

    override suspend fun readLong(): LongArray {
        val size = buffer.getPackedSize()
        val numElements = (size / 8).toInt()
        val outputAddr = buffer.lock()
        val results = LongArray(numElements)
        outputAddr.read(0, results, 0, numElements)
        buffer.unlock()
        return results
    }
}