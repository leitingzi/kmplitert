package com.leitz.kmplitert

actual class LiteRTCompiler {
    actual suspend fun init(filePath: String) {

    }

    actual suspend fun getInputBuffers(): List<TFBuffer> {
        TODO("Not yet implemented")
    }

    actual suspend fun getOutputBuffers(): List<TFBuffer> {
        TODO("Not yet implemented")
    }

    actual suspend fun run(inputs: List<TFBuffer>, outputs: List<TFBuffer>) {
    }

    actual suspend fun close() {
    }
}