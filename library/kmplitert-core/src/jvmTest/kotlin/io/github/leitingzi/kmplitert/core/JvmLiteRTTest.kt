package io.github.leitingzi.kmplitert.core

import io.github.leitingzi.kmplitert.core.model.LiteRtCompiledModel
import io.github.leitingzi.kmplitert.core.model.LiteRtEnvironment
import io.github.leitingzi.kmplitert.core.model.LiteRtHwAcceleratorSet
import io.github.leitingzi.kmplitert.core.model.LiteRtModel
import io.github.leitingzi.kmplitert.core.model.LiteRtOptions
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class JvmLiteRTTest {

    private val modelFilePath = "src/jvmTest/resources/CelsiusToFahrenheit.tflite"

    private fun createCompiledModel(
        accelerator: LiteRtHwAcceleratorSet = LiteRtHwAcceleratorSet.CPU
    ): LiteRtCompiledModel {
        return LiteRtCompiledModel.create(
            filePath = modelFilePath,
            accelerator = accelerator
        )
    }

    @Test
    fun shouldCreateAndDestroyEnvironment() {
        val env = LiteRtEnvironment.create()
        env.destroy()
    }

    @Test
    fun shouldLoadModelFromFile() {
        val model = LiteRtModel.create(modelFilePath)
        model.destroy()
    }

    @Test
    fun shouldSetCpuHardwareAccelerator() {
        val options = LiteRtOptions.create()
        options.setAccelerators(LiteRtHwAcceleratorSet.CPU)
        options.destroy()
    }

    @Test
    fun shouldCreateCompiledModelUsingCpuAccelerator() {
        val compiledModel = createCompiledModel()
        compiledModel.destroy()
    }

    @Test
    fun testGetCompiledModelBufferRequirements() {
        val compiledModel = createCompiledModel()
        compiledModel.getInputBufferRequirements(0, 0)
        compiledModel.getOutputBufferRequirements(0, 0)
        compiledModel.destroy()
    }

    @Test
    fun testGetCompiledModelTensorLayout() {
        val compiledModel = createCompiledModel()
        compiledModel.getInputTensorLayout(0, 0)
        compiledModel.getOutputTensorLayout(0, 1, false)
        compiledModel.destroy()
    }

    @Test
    fun testCreateManagedTensorBufferFromRequirements() {
        runTest {
            val compiledModel = createCompiledModel()

            val inputBuffers = compiledModel.getInputBuffers()
            inputBuffers[0].writeFloat(floatArrayOf(100f))

            val outputBuffers = compiledModel.getOutputBuffers()

            compiledModel.run(0, inputBuffers, outputBuffers)

            val result = outputBuffers[0].readFloat()

            println("Result = ${result[0]}")

            assertEquals(212f, result[0], 1f)

            compiledModel.destroy()
        }
    }

    @Test
    fun testExModel() = runTest {
        val compiledModel = createCompiledModel(
            accelerator = LiteRtHwAcceleratorSet.GPU
        )

        val inputBuffers = compiledModel.getInputBuffers()
        println("inputBuffersSize = ${inputBuffers.size}")

        inputBuffers[0].writeFloat(floatArrayOf(100f, 0f, -40f))

        val outputBuffers = compiledModel.getOutputBuffers()

        println("outputBuffersSize = ${outputBuffers.size}")

        compiledModel.run(0, inputBuffers, outputBuffers)

        val result = outputBuffers[0].readFloat()

        println("Result = ${result.contentToString()}")
    }
}