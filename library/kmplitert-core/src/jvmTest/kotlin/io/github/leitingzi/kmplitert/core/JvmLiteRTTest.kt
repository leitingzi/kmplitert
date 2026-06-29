package io.github.leitingzi.kmplitert.core

import io.github.leitingzi.kmplitert.core.model.*
import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.test.*

class JvmLiteRTTest {

    private val modelFilePath = "src/jvmTest/resources/CelsiusToFahrenheit.tflite"
    private val exModelFilePath = "src/jvmTest/resources/CelsiusToFahrenheitEx.tflite"

    @Test
    fun testEnvironmentLifecycle() {
        val env = LiteRtEnvironment.create()
        assertNotNull(env)
        env.destroy()
    }

    @Test
    fun testModelLoading() {
        val model = LiteRtModel.create(modelFilePath)
        assertNotNull(model)
        model.destroy()
    }

    @Test
    fun testModelLoadingFailure() {
        assertFailsWith<IllegalStateException> {
            LiteRtModel.create("non_existent_model.tflite")
        }
    }

    @Test
    fun testOptionsAndAccelerators() {
        val options = LiteRtOptions.create()
        assertNotNull(options)
        
        // Test CPU accelerator setting
        options.setAccelerators(LiteRtHwAcceleratorSet.CPU)
        
        // Test GPU accelerator setting (if supported by the JNA wrapper)
        // options.setAccelerators(LiteRtHwAcceleratorSet.GPU)
        
        options.destroy()
    }

    @Test
    fun testModelSignatureMetadata() {
        val model = LiteRtModel.create(modelFilePath)
        val signature = model.getSignature(0)
        
        assertTrue(signature.getNumInputs() > 0, "Model should have at least one input")
        assertTrue(signature.getNumOutputs() > 0, "Model should have at least one output")
        
        model.destroy()
    }

    @Test
    fun testCompiledModelCreation() {
        val compiledModel = LiteRtCompiledModel.create(modelFilePath, LiteRtHwAcceleratorSet.CPU)
        assertNotNull(compiledModel)
        compiledModel.destroy()
    }

    @Test
    fun testTensorLayoutAndRequirements() {
        val compiledModel = LiteRtCompiledModel.create(modelFilePath, LiteRtHwAcceleratorSet.CPU)
        
        val inputLayout = compiledModel.getInputTensorLayout(0, 0)
        assertNotNull(inputLayout)
        // We can check rank or dimensions if we know the model
        // assertEquals(1, inputLayout.rank) 
        
        val outputLayout = compiledModel.getOutputTensorLayout(0, 1, false)
        assertNotNull(outputLayout)

        val inputReq = compiledModel.getInputBufferRequirements(0, 0)
        assertNotNull(inputReq)
        
        val outputReq = compiledModel.getOutputBufferRequirements(0, 0)
        assertNotNull(outputReq)

        compiledModel.destroy()
    }

    @Test
    fun testSingleInference() = runTest {
        val compiledModel = LiteRtCompiledModel.create(modelFilePath, LiteRtHwAcceleratorSet.CPU)

        val inputBuffers = compiledModel.getInputBuffers()
        assertEquals(1, inputBuffers.size)
        
        // 100 Celsius
        inputBuffers[0].writeFloat(floatArrayOf(100f))

        val outputBuffers = compiledModel.getOutputBuffers()
        assertEquals(1, outputBuffers.size)

        compiledModel.run(0, inputBuffers, outputBuffers)

        val result = outputBuffers[0].readFloat()
        
        // 100C = 212F (Tolerance increased due to model training deviations)
        assertEquals(212f, result[0], 5.0f)

        compiledModel.destroy()
    }

    @Test
    fun testMultiValueInference() = runTest {
        // CelsiusToFahrenheitEx.tflite usually supports batch or multiple inputs/outputs
        val file = File(exModelFilePath)
        if (!file.exists()) {
            println("Skipping testMultiValueInference: $exModelFilePath not found")
            return@runTest
        }

        val compiledModel = LiteRtCompiledModel.create(exModelFilePath, LiteRtHwAcceleratorSet.CPU)

        val inputBuffers = compiledModel.getInputBuffers()
        val outputBuffers = compiledModel.getOutputBuffers()

        println("Ex Model Inputs: ${inputBuffers.size}, Outputs: ${outputBuffers.size}")

        // Assuming it's a batch of 3: 100C, 0C, -40C
        val testInputs = floatArrayOf(100f, 0f, -40f)
        inputBuffers[0].writeFloat(testInputs)

        compiledModel.run(0, inputBuffers, outputBuffers)

        val result = outputBuffers[0].readFloat()
        println("Ex Model Results: ${result.contentToString()}")

        // 100C -> 212F, 0C -> 32F, -40C -> -40F
        assertEquals(212f, result[0], 5.0f)
        assertEquals(32f, result[1], 5.0f)
        assertEquals(-40f, result[2], 5.0f)

        compiledModel.destroy()
    }

    @Test
    fun testRepeatedInference() = runTest {
        val compiledModel = LiteRtCompiledModel.create(modelFilePath, LiteRtHwAcceleratorSet.CPU)
        val inputBuffers = compiledModel.getInputBuffers()
        val outputBuffers = compiledModel.getOutputBuffers()

        val testCases = mapOf(
            100f to 212f,
            0f to 32f,
            -40f to -40f,
            37f to 98.6f
        )

        testCases.forEach { (celsius, expectedFahrenheit) ->
            inputBuffers[0].writeFloat(floatArrayOf(celsius))
            compiledModel.run(0, inputBuffers, outputBuffers)
            val result = outputBuffers[0].readFloat()
            println("Input: $celsius C, Expected: $expectedFahrenheit F, Actual: ${result[0]} F")
            assertEquals(expectedFahrenheit, result[0], 5.0f)
        }

        compiledModel.destroy()
    }
}
