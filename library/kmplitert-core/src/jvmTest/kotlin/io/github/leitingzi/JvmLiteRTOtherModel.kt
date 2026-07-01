package io.github.leitingzi

import io.github.leitingzi.model.LiteRtCompiledModel
import io.github.leitingzi.model.LiteRtHwAcceleratorSet
import kotlin.test.Test


class JvmLiteRTOtherModel {

    private val modelFilePath = "src/jvmTest/resources/mobilenet_v1.tflite"
    @Test
    fun testModelLoading() {
        val compiledModel = LiteRtCompiledModel.create(
            filePath = modelFilePath,
            accelerator = LiteRtHwAcceleratorSet.CPU
        )
        val inputs = compiledModel.getInputBuffers()
        println("inputs = ${inputs.size}")

        val outputs = compiledModel.getOutputBuffers()
        println("outputs = ${outputs.size}")
    }
}