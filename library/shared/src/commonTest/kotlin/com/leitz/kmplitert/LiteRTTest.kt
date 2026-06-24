package com.leitz.kmplitert

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LiteRTTest {

    private val testFilePath = "src/commonTest/resources/kotlin/CelsiusToFahrenheit.tflite"

    @Test
    fun runModel() {
        runTest {
            val compiler = LiteRTCompiler()
            compiler.init(testFilePath)

            val inputs = compiler.getInputBuffers()
            val outputs = compiler.getOutputBuffers()

            inputs[0].writeFloat(floatArrayOf(100f))
            compiler.run(inputs, outputs)

            println(outputs[0].readFloat().contentToString())
        }
    }
}