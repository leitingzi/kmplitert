package com.leitz.kmplitert

import com.leitz.kmplitert.model.LiteRtCompiledModel
import com.leitz.kmplitert.model.LiteRtEnvironment
import com.leitz.kmplitert.model.LiteRtHwAcceleratorSet
import com.leitz.kmplitert.model.LiteRtModel
import com.leitz.kmplitert.model.LiteRtOptions
import com.leitz.kmplitert.model.LiteRtTensorBufferType
import com.leitz.kmplitert.model.LiteRtRankedTensorType
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
        compiledModel.getOutputTensorLayout(0, 1)
        compiledModel.destroy()
    }

    @Test
    fun testCreateManagedTensorBufferFromRequirements() {
        val env = LiteRtEnvironment.create()
        val compiledModel = LiteRtCompiledModel.create(
            env = env,
            filePath = modelFilePath
        )

        val inputBufferRequirements = compiledModel.getInputBufferRequirements(
            signature_index = 0,
            input_index = 0
        )
        val outputBufferRequirements = compiledModel.getOutputBufferRequirements(
            signature_index = 0,
            output_index = 0
        )

        val inputLayout = compiledModel.getInputTensorLayout(0, 0)
        val outputLayout = compiledModel.getOutputTensorLayout(0, 1)

        val inputRankedType = LiteRtRankedTensorType()
        inputRankedType.elementType = LITERT_ELEMENT_TYPE_FLOAT32
        // 直接从 inputLayout 拷贝数据
        inputRankedType.layout.flags = inputLayout.flags
        if (com.sun.jna.Platform.isWindows()) {
            inputRankedType.layout.padding = inputLayout.padding
        }
        for (i in 0 until 8) {
            inputRankedType.layout.dimensions[i] = inputLayout.dimensions[i]
            inputRankedType.layout.strides[i] = inputLayout.strides[i]
        }
        inputRankedType.write()

        val intputTensorBuffer = compiledModel.createManagedTensorBufferFromRequirements(
            env = env,
            tensor_type = inputRankedType,
            requirements = inputBufferRequirements
        )

        println("Input Tensor Buffer: $intputTensorBuffer")

        val outputRankedType = LiteRtRankedTensorType()
        outputRankedType.elementType = LITERT_ELEMENT_TYPE_FLOAT32
        outputRankedType.layout.flags = outputLayout.flags
        if (com.sun.jna.Platform.isWindows()) {
            outputRankedType.layout.padding = outputLayout.padding
        }
        for (i in 0 until 8) {
            outputRankedType.layout.dimensions[i] = outputLayout.dimensions[i]
            outputRankedType.layout.strides[i] = outputLayout.strides[i]
        }
        outputRankedType.write()

        val outputTensorBuffer = compiledModel.createManagedTensorBufferFromRequirements(
            env = env,
            tensor_type = outputRankedType,
            requirements = outputBufferRequirements
        )

        println("Output Tensor Buffer: $outputTensorBuffer")

        val floats = floatArrayOf(100f)
        val inputAddr = intputTensorBuffer.lock()
        inputAddr.write(0, floats, 0, floats.size)
        intputTensorBuffer.unlock()

        compiledModel.run(
            0,
            1,
            intputTensorBuffer,
            1,
            outputTensorBuffer,
        )

        val outputAddr = outputTensorBuffer.lock()
        val results = FloatArray(1)
        outputAddr.read(0, results, 0, 1)
        outputTensorBuffer.unlock()

        println("Result = ${results[0]}")

        assertEquals(212f, results[0], 1f)

        compiledModel.destroy()
    }
}