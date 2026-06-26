@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.leitz.kmplitert

actual class LiteRTCompiler actual constructor(val filePath: String) {
    actual suspend fun init() {
        TODO("Not yet implemented")
    }

    actual suspend fun getInputBuffers(): List<TFBuffer> {
        TODO("Not yet implemented")
    }

    actual suspend fun getOutputBuffers(): List<TFBuffer> {
        TODO("Not yet implemented")
    }

    actual suspend fun run(inputs: List<TFBuffer>, outputs: List<TFBuffer>) {
        TODO("Not yet implemented")
    }

    actual suspend fun close() {
        TODO("Not yet implemented")
    }
}