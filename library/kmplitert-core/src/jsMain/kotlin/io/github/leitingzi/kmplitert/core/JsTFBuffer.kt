package io.github.leitingzi.kmplitert.core

import io.github.leitingzi.kmplitert.core.model.Tensor
import kotlinx.coroutines.await
import org.khronos.webgl.Float32Array
import org.khronos.webgl.Int32Array
import org.khronos.webgl.toFloat32Array
import org.khronos.webgl.toFloatArray

class JsTFBuffer(val shape: Int32Array) : TFBuffer {

    lateinit var tensor: Tensor

    override fun writeInt(data: IntArray) {
        TODO("Not yet implemented")
    }
    override fun writeFloat(data: FloatArray) {
        val data = data.toFloat32Array()
        tensor = Tensor(data, shape)
    }
    override fun writeInt8(data: ByteArray) {
        TODO("Not yet implemented")
    }
    override fun writeBoolean(data: BooleanArray) {
        TODO("Not yet implemented")
    }
    override fun writeLong(data: LongArray) {
        TODO("Not yet implemented")
    }


    override suspend fun readInt(): IntArray {
        TODO("Not yet implemented")
    }
    override suspend fun readFloat(): FloatArray {
        val data = tensor.data().await()
        val float32Array = data as Float32Array
        return float32Array.toFloatArray()
    }
    override suspend fun readInt8(): ByteArray {
        TODO("Not yet implemented")
    }
    override suspend fun readBoolean(): BooleanArray {
        TODO("Not yet implemented")
    }
    override suspend fun readLong(): LongArray {
        TODO("Not yet implemented")
    }
}