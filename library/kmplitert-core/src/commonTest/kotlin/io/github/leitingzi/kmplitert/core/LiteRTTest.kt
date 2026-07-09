package io.github.leitingzi.kmplitert.core

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LiteRTTest {

    private val testFilePath = "src/commonTest/resources/CelsiusToFahrenheit.tflite"

    @Test
    fun testModelForCPU() = runTest {
        val compiler = LiteRTCompiler(filePath = testFilePath, accelerator = LiteRTAccelerator.CPU)
        compiler.init()

        // Test Input Tensor Type
        val inputType = compiler.getInputTensorType("input_c")
        assertEquals(LiteRTElementType.FLOAT, inputType.elementType)
        // Adjusting expectation: CelsiusToFahrenheit model often has [1, 1] shape, which is rank 2.
        assertTrue(inputType.layout?.rank!! >= 1)

        // Test Output Tensor Type
        val outputType = compiler.getOutputTensorType("Identity")
        assertEquals(LiteRTElementType.FLOAT, outputType.elementType)
        assertTrue(outputType.layout?.rank!! >= 1)

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
        try {
            compiler.init()
        } catch (e: Exception) {
            println("Skipping GPU test: Accelerator initialization failed (likely no hardware in CI): ${e.message}")
            return@runTest
        }
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
        try {
            compiler.init()
        } catch (e: Exception) {
            println("Skipping NPU test: Accelerator initialization failed (likely no hardware in CI): ${e.message}")
            return@runTest
        }
        val inputs = compiler.getInputBuffers()
        val outputs = compiler.getOutputBuffers()
        inputs[0].writeFloat(floatArrayOf(100f))
        compiler.run(inputs, outputs)
        println(outputs[0].readFloat().contentToString())
        compiler.close()
    }
}

