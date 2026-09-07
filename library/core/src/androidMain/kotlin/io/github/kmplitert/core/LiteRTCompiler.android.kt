package io.github.kmplitert.core

import com.google.ai.edge.litert.CompiledModel
import com.google.ai.edge.litert.TensorBuffer

actual class LiteRTCompiler {
    private lateinit var compiledModel: CompiledModel

    fun createInputBuffers(): List<LiteRTBuffer> {
        return compiledModel.createInputBuffers().map { Buffer(it) }
    }

    fun createOutputBuffers(): List<LiteRTBuffer>  {
        return compiledModel.createOutputBuffers().map { Buffer(it) }
    }

    fun run(inputs: List<LiteRTBuffer>, outputs: List<LiteRTBuffer>) {
        compiledModel.run(
            inputs.map { (it as Buffer).buffer },
            outputs.map { (it as Buffer).buffer }
        )
    }

    fun close() {
        compiledModel.close()
    }

    private class Buffer(val buffer: TensorBuffer): LiteRTBuffer {
        override fun writeBoolean(data: BooleanArray) {
            buffer.writeBoolean(data)
        }

        override fun writeByte(data: ByteArray) {
            buffer.writeInt8(data)
        }

        override fun writeInt(data: IntArray) {
            buffer.writeInt(data)
        }

        override fun writeFloat(data: FloatArray) {
            buffer.writeFloat(data)
        }

        override fun writeLong(data: LongArray) {
            buffer.writeLong(data)
        }

        override fun readBoolean(): BooleanArray {
            return buffer.readBoolean()
        }

        override fun readByte(): ByteArray {
            return buffer.readInt8()
        }

        override fun readInt(): IntArray {
            return buffer.readInt()
        }

        override fun readFloat(): FloatArray {
            return buffer.readFloat()
        }

        override fun readLong(): LongArray {
            return buffer.readLong()
        }
    }
}