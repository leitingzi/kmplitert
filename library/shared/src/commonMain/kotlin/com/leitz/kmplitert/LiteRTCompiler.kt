package com.leitz.kmplitert

expect class LiteRTCompiler() {
    suspend fun init(filePath: String)
    suspend fun getInputBuffers(): List<TFBuffer>
    suspend fun getOutputBuffers(): List<TFBuffer>
    suspend fun run(inputs: List<TFBuffer>, outputs: List<TFBuffer>)
    suspend fun close()
}