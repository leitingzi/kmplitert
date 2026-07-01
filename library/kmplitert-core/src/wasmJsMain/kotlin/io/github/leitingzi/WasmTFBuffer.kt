@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.leitingzi

import kotlinx.coroutines.await
import org.khronos.webgl.*

class WasmTFBuffer(val shape: Int32Array) : TFBuffer {

    lateinit var tensor: io.github.leitingzi.model.Tensor

    override fun writeInt(data: IntArray) {
        val int32Array = Int32Array(data.size)
        for (i in data.indices) {
            int32Array[i] = data[i]
        }
        tensor = _root_ide_package_.io.github.leitingzi.model.Tensor(int32Array, shape)
    }

    override fun writeFloat(data: FloatArray) {
        tensor = _root_ide_package_.io.github.leitingzi.model.Tensor(data.toFloat32Array(), shape)
    }

    override fun writeInt8(data: ByteArray) {
        val int8Array = Int8Array(data.size)
        for (i in data.indices) {
            int8Array[i] = data[i]
        }
        tensor = _root_ide_package_.io.github.leitingzi.model.Tensor(int8Array, shape)
    }

    override fun writeBoolean(data: BooleanArray) {
        val uint8Array = Uint8Array(data.size)
        for (i in data.indices) {
            uint8Array[i] = if (data[i]) 1.toByte() else 0.toByte()
        }
        tensor = _root_ide_package_.io.github.leitingzi.model.Tensor(uint8Array, shape)
    }

    override fun writeLong(data: LongArray) {
        val bigInt64Array = createBigInt64Array(data.size)
        for (i in data.indices) {
            setBigInt64Array(bigInt64Array, i, data[i])
        }
        tensor = _root_ide_package_.io.github.leitingzi.model.Tensor(
            bigInt64Array.unsafeCast<ArrayBufferView>(),
            shape
        )
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
        val int8Array = data as Int8Array
        val result = ByteArray(int8Array.length)
        for (i in 0 until int8Array.length) {
            result[i] = int8Array[i]
        }
        return result
    }

    override suspend fun readBoolean(): BooleanArray {
        val data = tensor.data().await()
        val uint8Array = data as Uint8Array
        val result = BooleanArray(uint8Array.length)
        for (i in 0 until uint8Array.length) {
            result[i] = getUint8ArrayElement(
                uint8Array,
                i
            ) != 0
        }
        return result
    }

    @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
    override suspend fun readLong(): LongArray {
        val data = tensor.data().await()
        val bigInt64Array = data as JsAny
        val length = getBigInt64ArrayLength(bigInt64Array)
        val result = LongArray(length)
        for (i in 0 until length) {
            result[i] = getBigInt64Array(bigInt64Array, i)
        }
        return result
    }
}
