package io.github.leitingzi.kmplitert.core

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class NativeLiteRTTest {

    // Assumes this file exists in the current directory of the test runtime environment
    // Note: Native tests typically require manually placing resource files in the same directory as the executable
    // TODO IOS Simulator testing encounters issues where model file paths cannot be found.
    private val testFilePath = "src/commonTest/resources/CelsiusToFahrenheit.tflite"

    @Test
    fun testNativeModelInference() = runTest {
        val compiler = LiteRTCompiler(filePath = testFilePath, accelerator = LiteRTAccelerator.CPU)
        
        try {
            compiler.init()
            val inputs = compiler.getInputBuffers()
            val outputs = compiler.getOutputBuffers()
            
            assertTrue(inputs.isNotEmpty(), "Input buffers should not be empty")
            assertTrue(outputs.isNotEmpty(), "Output buffers should not be empty")

            // Test Input Tensor Type
            val inputType = compiler.getInputTensorType("input_c")
            assertTrue(inputType.elementType == LiteRTElementType.FLOAT, "Input element type should be FLOAT")
            assertTrue(inputType.layout?.rank!! >= 1, "Input rank should be >= 1")

            // Test Output Tensor Type
            val outputType = compiler.getOutputTensorType("Identity")
            assertTrue(outputType.elementType == LiteRTElementType.FLOAT, "Output element type should be FLOAT")
            assertTrue(outputType.layout?.rank!! >= 1, "Output rank should be >= 1")

            // 写入测试数据 (100摄氏�?
            inputs[0].writeFloat(floatArrayOf(100f))

            // 执行推理
            compiler.run(inputs, outputs)

            // 读取结果 (应接�?212 华氏�?
            val result = outputs[0].readFloat()
            println("Native Inference Result: ${result.contentToString()}")
            
            assertTrue(result[0] > 200f && result[0] < 220f, "Inference result should be around 212")
            
        } catch (e: Exception) {
            println("Native test failed: ${e.message}")
            // In certain CI environments, this may fail if library files are not configured correctly; we throw an error here so the test fails.
            throw e
        } finally {
            compiler.close()
        }
    }
}


