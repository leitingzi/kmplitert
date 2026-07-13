package io.github.leitingzi.kmplitert.core

import io.github.leitingzi.kmplitert.core.model.Tensor
import kotlinx.coroutines.await
import org.khronos.webgl.*
import org.khronos.webgl.get

class JsTFBuffer(private val jsShape: Int32Array, private val dtype: String) : TFBuffer {

    var tensor: Tensor? = null

    private fun checkTensor(): Tensor {
        return tensor ?: throw IllegalStateException("Tensor is not initialized. Call write*() or run model first.")
    }

    override fun writeInt(data: IntArray) {
        val int32Array = Int32Array(data.size)
        int32Array.asDynamic().set(data)
        tensor = Tensor(int32Array, jsShape)
    }

    override fun writeFloat(data: FloatArray) {
        tensor = Tensor(data.toFloat32Array(), jsShape)
    }

    override fun writeInt8(data: ByteArray) {
        val uint8Array = Uint8Array(data.size)
        uint8Array.asDynamic().set(data)
        tensor = Tensor(uint8Array, jsShape)
    }

    override fun writeBoolean(data: BooleanArray) {
        val uint8Array = Uint8Array(data.size)
        for (i in data.indices) {
            uint8Array[i] = if (data[i]) 1.toByte() else 0.toByte()
        }
        tensor = Tensor(uint8Array, jsShape)
    }

    override fun writeLong(data: LongArray) {
        val bigInt64Array = js("new BigInt64Array(data.length)")
        bigInt64Array.set(data)
        tensor = Tensor(bigInt64Array.unsafeCast<ArrayBufferView>(), jsShape)
    }


    override suspend fun readInt(): IntArray {
        val data = checkTensor().data().await()
        val int32Array = data as Int32Array
        val result = IntArray(int32Array.length)
        result.asDynamic().set(int32Array)
        return result
    }

    override suspend fun readFloat(): FloatArray {
        val data = checkTensor().data().await()
        val float32Array = data as Float32Array
        return float32Array.toFloatArray()
    }

    override suspend fun readInt8(): ByteArray {
        val data = checkTensor().data().await()
        val uint8Array = data as Uint8Array
        val result = ByteArray(uint8Array.length)
        result.asDynamic().set(uint8Array)
        return result
    }

    override suspend fun readBoolean(): BooleanArray {
        val data = checkTensor().data().await()
        val array = data.asDynamic()
        val length = array.length.unsafeCast<Int>()
        val result = BooleanArray(length)
        for (i in 0 until length) {
            result[i] = array[i].unsafeCast<Int>() != 0
        }
        return result
    }

    override suspend fun readLong(): LongArray {
        val data = checkTensor().data().await()
        val bigInt64Array = data.asDynamic()
        val length = bigInt64Array.length.unsafeCast<Int>()
        val result = LongArray(length)
        for (i in 0 until length) {
            result[i] = bigInt64Array[i].unsafeCast<Long>()
        }
        return result
    }
}


