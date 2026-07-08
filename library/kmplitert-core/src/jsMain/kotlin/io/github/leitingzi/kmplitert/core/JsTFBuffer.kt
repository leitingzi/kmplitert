package io.github.leitingzi.kmplitert.core

import io.github.leitingzi.kmplitert.core.model.Tensor
import kotlinx.coroutines.await
import org.khronos.webgl.*
import org.khronos.webgl.get

class JsTFBuffer(private val jsShape: Int32Array, private val dtype: String) : TFBuffer {

    lateinit var tensor: Tensor

    override fun writeInt(data: IntArray) {
        val int32Array = Int32Array(data.size)
        for (i in data.indices) {
            int32Array[i] = data[i]
        }
        tensor = Tensor(int32Array, jsShape)
    }

    override fun writeFloat(data: FloatArray) {
        tensor = Tensor(data.toFloat32Array(), jsShape)
    }

    override fun writeInt8(data: ByteArray) {
        // If the model expects uint8 or int8, try using Uint8Array.
        // Some runtimes (like @litertjs/core) might not support Int8Array specifically.
        val uint8Array = Uint8Array(data.size)
        for (i in data.indices) {
            uint8Array[i] = data[i]
        }
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
        for (i in data.indices) {
            bigInt64Array[i] = data[i]
        }
        tensor = Tensor(bigInt64Array.unsafeCast<ArrayBufferView>(), jsShape)
    }


    override suspend fun readInt(): IntArray {
        val data = tensor.data().await()
        val int32Array = data as Int32Array
        val result = IntArray(int32Array.length)
        for (i in 0 until int32Array.length) {
            result[i] = int32Array[i]
        }
        return result
    }

    override suspend fun readFloat(): FloatArray {
        val data = tensor.data().await()
        val float32Array = data as Float32Array
        return float32Array.toFloatArray()
    }

    override suspend fun readInt8(): ByteArray {
        val data = tensor.data().await()
        if (data is Uint8Array) {
            val result = ByteArray(data.length)
            for (i in 0 until data.length) {
                result[i] = data[i]
            }
            return result
        }
        // Fallback or dynamic handling
        val array = data.asDynamic()
        val result = ByteArray(array.length.unsafeCast<Int>())
        for (i in 0 until result.size) {
            result[i] = array[i].unsafeCast<Byte>()
        }
        return result
    }

    override suspend fun readBoolean(): BooleanArray {
        val data = tensor.data().await()
        if (data is Uint8Array) {
            val result = BooleanArray(data.length)
            for (i in 0 until data.length) {
                result[i] = data[i] != 0.toByte()
            }
            return result
        }
        val array = data.asDynamic()
        val result = BooleanArray(array.length.unsafeCast<Int>())
        for (i in 0 until result.size) {
            result[i] = array[i].unsafeCast<Int>() != 0
        }
        return result
    }

    override suspend fun readLong(): LongArray {
        val data = tensor.data().await()
        val bigInt64Array = data.asDynamic()
        val result = LongArray(bigInt64Array.length.unsafeCast<Int>())
        for (i in result.indices) {
            result[i] = bigInt64Array[i].unsafeCast<Long>()
        }
        return result
    }
}


