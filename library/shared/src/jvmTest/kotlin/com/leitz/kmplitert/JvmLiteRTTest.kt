package com.leitz.kmplitert

import com.sun.jna.ptr.PointerByReference
import junit.framework.TestCase.assertEquals
import kotlin.test.Test
import kotlin.test.assertNotNull

class JvmLiteRTTest {

    private val lib = LibLiteRt.INSTANCE

    private val modelFilePath = "src/jvmTest/resources/CelsiusToFahrenheit.tflite"

    @Test
    fun shouldCreateAndDestroyEnvironment() {
        val envRef = PointerByReference()

        val status = lib.LiteRtCreateEnvironment(
            numOptions = 0,
            options = null,
            environment = envRef
        )

        assertEquals(0, status)

        val environment = envRef.value
        assertNotNull(environment)

        println("Environment = $environment")

        lib.LiteRtDestroyEnvironment(environment)
    }

    @Test
    fun shouldLoadModelFromFile() {
        val modelRef = PointerByReference()

        val status = lib.LiteRtCreateModelFromFile(modelFilePath, modelRef)

        assertEquals(0, status)

        val model = modelRef.value
        assertNotNull(model)

        println("Model = $model")

        lib.LiteRtDestroyModel(model)
    }

    @Test
    fun shouldCreateAndDestroyOptions() {
        val optionsRef = PointerByReference()

        val status = lib.LiteRtCreateOptions(optionsRef)

        assertEquals(0, status)

        val options = optionsRef.value
        assertNotNull(options)

        println("Options = $options")

        lib.LiteRtDestroyOptions(options)
    }

    @Test
    fun shouldSetCpuHardwareAccelerator() {
        val optionsRef = PointerByReference()

        assertEquals(0, lib.LiteRtCreateOptions(optionsRef))

        val options = optionsRef.value

        val status = lib.LiteRtSetOptionsHardwareAccelerators(
            options,
            LITERT_ACCELERATOR_CPU
        )

        assertEquals(0, status)

        lib.LiteRtDestroyOptions(options)
    }

    @Test
    fun shouldCreateCompiledModelUsingCpuAccelerator() {
        // Environment
        val envRef = PointerByReference()
        assertEquals(
            0,
            lib.LiteRtCreateEnvironment(
                0,
                null,
                envRef
            )
        )
        val environment = envRef.value

        // Model
        val modelRef = PointerByReference()
        assertEquals(0, lib.LiteRtCreateModelFromFile(modelFilePath, modelRef))

        val model = modelRef.value

        // Options
        val optionsRef = PointerByReference()
        assertEquals(
            0,
            lib.LiteRtCreateOptions(optionsRef)
        )
        val options = optionsRef.value

        assertEquals(
            0,
            lib.LiteRtSetOptionsHardwareAccelerators(options, LITERT_ACCELERATOR_CPU)
        )

        // CompiledModel
        val compiledRef = PointerByReference()

        val status = lib.LiteRtCreateCompiledModel(
            environment = environment,
            model = model,
            compilationOptions = options,
            compiledModel = compiledRef
        )

        assertEquals(0, status)

        val compiledModel = compiledRef.value
        assertNotNull(compiledModel)

        println("CompiledModel = $compiledModel")

        // Cleanup
        lib.LiteRtDestroyCompiledModel(compiledModel)
        lib.LiteRtDestroyOptions(options)
        lib.LiteRtDestroyModel(model)
        lib.LiteRtDestroyEnvironment(environment)
    }

    @Test
    fun testLiteRtLayout() {
        val layout = LiteRtLayout()
        layout.apply {
            setRank(4)
            setHasStrides(true)

            dimensions[0] = 1
            dimensions[1] = 224
            dimensions[2] = 224
            dimensions[3] = 3

            strides[0] = 150528
            strides[1] = 672
            strides[2] = 3
            strides[3] = 1

            write()
        }
        println("layout = ${layout.pointer}")
    }

    @Test
    fun shouldLiteRTUtils() {
        val env = LiteRtUtils.createEnvironment()
        val model = LiteRtUtils.createModel(modelFilePath)
        val options = LiteRtUtils.createOptions()
        val compiledModel = LiteRtUtils.createCompiledModel(
            environment = env,
            model = model,
            options = options
        )

        println("compiledModel ptr = $compiledModel")

        val inputBufferRequirements = LiteRtUtils.getInputBufferRequirements(
            compiledModel = compiledModel,
            signatureIndex = 0,
            inputIndex = 0
        )

        println("inputBufferRequirements = $inputBufferRequirements")

        val outputBufferRequirements = LiteRtUtils.getOutputBufferRequirements(
            compiledModel = compiledModel,
            signatureIndex = 0,
            outputIndex = 0
        )

        println("outputBufferRequirements = $outputBufferRequirements")

        val layout = LiteRtUtils.getInputTensorLayout(
            compiledModel = compiledModel,
            signatureIndex = 0,
            inputIndex = 0
        )

        println("layout = ${layout.pointer}")

        val outputLayouts = LiteRtUtils.getOutputTensorLayout(
            compiledModel = compiledModel,
            signatureIndex = 0,
            numLayouts = 1,
            updateAllocation = true
        )

        println("outputLayouts = ${outputLayouts.pointer}")

        val inputRankedType = LiteRtRankedTensorType()
        inputRankedType.elementType = LITERT_ELEMENT_TYPE_FLOAT32
        inputRankedType.layout.flags = layout.flags
        for (i in 0 until 8) {
            inputRankedType.layout.dimensions[i] = layout.dimensions[i]
            inputRankedType.layout.strides[i] = layout.strides[i]
        }
        inputRankedType.write()

        val inputTensorBuffer = LiteRtUtils.createTensorBufferFromRequirements(
            env = env,
            tensorType = inputRankedType,
            requirements = inputBufferRequirements
        )

        println("inputTensorBuffer = $inputTensorBuffer")

        val floats = floatArrayOf(100f)
        val inputAddr = LiteRtUtils.lock(inputTensorBuffer, LITERT_TENSOR_BUFFER_LOCK_MODE_WRITE)
        inputAddr.write(0, floats, 0, floats.size)
        LiteRtUtils.unlock(inputTensorBuffer)

        val outputTensorBuffer = LiteRtUtils.createTensorBufferFromRequirements(
            env = env,
            tensorType = inputRankedType,
            requirements = outputBufferRequirements
        )

        println("outputTensorBuffer = $outputTensorBuffer")


        LiteRtUtils.run(
            compiledModel = compiledModel,
            signatureIndex = 0,
            numInputBuffers = 1,
            inputBuffers = inputTensorBuffer,
            numOutputBuffers = 1,
            outputBuffers = outputTensorBuffer
        )

        val outputAddr = LiteRtUtils.lock(outputTensorBuffer, LITERT_TENSOR_BUFFER_LOCK_MODE_READ)
        val results = FloatArray(1)
        outputAddr.read(0, results, 0, 1)
        LiteRtUtils.unlock(outputTensorBuffer)

        println("Result = ${results[0]}")
        assertEquals(212f, results[0], 1f)


        lib.LiteRtDestroyTensorBuffer(inputTensorBuffer)
        lib.LiteRtDestroyTensorBuffer(outputTensorBuffer)


        LiteRtUtils.destroy(env, model, options, compiledModel)
    }
}