package com.leitz.kmplitert

import com.sun.jna.Pointer

actual class LiteRTCompiler {

    private val env = LiteRtUtils.createEnvironment()
    private lateinit var model: Pointer
    private val option = LiteRtUtils.createOptions()

    private lateinit var compiledModel: Pointer

    actual suspend fun init(filePath: String) {
        model = LiteRtUtils.createModel(filePath)
        compiledModel = LiteRtUtils.createCompiledModel(
            environment = env,
            model = model,
            options = option
        )
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
        LiteRtUtils.destroy(
            environment = env,
            model = model,
            options = option,
            compiledModel = compiledModel
        )
    }
}