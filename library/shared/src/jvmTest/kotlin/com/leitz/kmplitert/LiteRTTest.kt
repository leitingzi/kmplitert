package com.leitz.kmplitert

import com.sun.jna.ptr.PointerByReference
import junit.framework.TestCase.assertEquals
import kotlin.test.Test
import kotlin.test.assertNotNull

class LiteRTTest {

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
    fun shouldLiteRTUtils() {
        val env = LiteRtUtils.createEnvironment()
        val model = LiteRtUtils.createModel(modelFilePath)
        val options = LiteRtUtils.createOptions()
        val compiledModel = LiteRtUtils.createCompiledModel(env, model, options)
        LiteRtUtils.destroy(env, model, options, compiledModel)
    }
}