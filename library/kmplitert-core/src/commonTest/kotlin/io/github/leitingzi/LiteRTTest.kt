package io.github.leitingzi

import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class LiteRTTest {

    private val testFilePath = "src/commonTest/resources/CelsiusToFahrenheit.tflite"

    @Test
    fun testModelForCPU() = runTest {
        val compiler = LiteRTCompiler(filePath = testFilePath, accelerator = LiteRTAccelerator.CPU)
        compiler.init()
        val inputs = compiler.getInputBuffers()
        val outputs = compiler.getOutputBuffers()
        inputs[0].writeFloat(floatArrayOf(100f))
        compiler.run(inputs, outputs)
        println(outputs[0].readFloat().contentToString())
        compiler.close()
    }

    @Test
    fun testModelForGPU() = runTest {
        val compiler = LiteRTCompiler(filePath = testFilePath, accelerator = LiteRTAccelerator.GPU)
        compiler.init()
        val inputs = compiler.getInputBuffers()
        val outputs = compiler.getOutputBuffers()
        inputs[0].writeFloat(floatArrayOf(100f))
        compiler.run(inputs, outputs)
        println(outputs[0].readFloat().contentToString())
        compiler.close()
    }

    @Test
    fun testModelForNPU() = runTest {
        val compiler = LiteRTCompiler(filePath = testFilePath, accelerator = LiteRTAccelerator.NPU)
        compiler.init()
        val inputs = compiler.getInputBuffers()
        val outputs = compiler.getOutputBuffers()
        inputs[0].writeFloat(floatArrayOf(100f))
        compiler.run(inputs, outputs)
        println(outputs[0].readFloat().contentToString())
        compiler.close()
    }
}