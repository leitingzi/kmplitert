package com.leitz.kmplitert

import com.leitz.kmplitert.model.*
import junit.framework.TestCase.assertEquals
import kotlin.test.Test

class JvmLiteRTNewTest {

    private val modelFilePath = "src/jvmTest/resources/CelsiusToFahrenheit.tflite"

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
        val compiledModel = LiteRtCompiledModel.create(filePath = modelFilePath)
        compiledModel.destroy()
    }

    @Test
    fun testGetCompiledModelBufferRequirements() {
        val compiledModel = LiteRtCompiledModel.create(filePath = modelFilePath)
        compiledModel.getInputBufferRequirements(0, 0)
        compiledModel.getOutputBufferRequirements(0, 0)
        compiledModel.destroy()
    }

    @Test
    fun testGetCompiledModelTensorLayout() {
        val compiledModel = LiteRtCompiledModel.create(filePath = modelFilePath)
        compiledModel.getInputTensorLayout(0, 0)
        compiledModel.getOutputTensorLayout(0, 1, false)
        compiledModel.destroy()
    }

    @Test
    fun testCreateManagedTensorBufferFromRequirements() {
        val compiledModel = LiteRtCompiledModel.create(filePath = modelFilePath)

        val inputBuffers = compiledModel.getInputBuffers()
        inputBuffers[0].writeFloat(floatArrayOf(100f))

        val outputBuffers = compiledModel.getOutputBuffers()

        compiledModel.run(0, inputBuffers, outputBuffers)

        val result = outputBuffers[0].readFloat()

        println("Result = ${result[0]}")

        assertEquals(212f, result[0], 1f)

        compiledModel.destroy()
    }

    @Test
    fun testExModel() {
        val modelPath = "src/jvmTest/resources/CelsiusToFahrenheitEx.tflite"
        val compiledModel = LiteRtCompiledModel.create(filePath = modelPath)
        val inputBuffers = compiledModel.getInputBuffers()
        println("inputBuffersSize = ${inputBuffers.size}")

        inputBuffers[0].writeFloat(floatArrayOf(100f, 0f, -40f))

        val outputBuffers = compiledModel.getOutputBuffers()

        println("outputBuffersSize = ${outputBuffers.size}")

        compiledModel.run(0, inputBuffers, outputBuffers)

        val result = outputBuffers[0].readFloat()

        println("Result = ${result[0]}")
        println("Result = ${result[1]}")
        println("Result = ${result[2]}")
        println("ResultSize = ${result.size}")
    }
}