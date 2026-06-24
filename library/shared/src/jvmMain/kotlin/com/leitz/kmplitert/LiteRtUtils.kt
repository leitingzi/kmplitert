package com.leitz.kmplitert

import com.sun.jna.Pointer
import com.sun.jna.ptr.PointerByReference

object LiteRtUtils {
    private val lib = LibLiteRt.INSTANCE

    fun createEnvironment(): Pointer {
        val envRef = PointerByReference()
        lib.LiteRtCreateEnvironment(numOptions = 0, options = null, environment = envRef)
        return envRef.value
    }

    fun createModel(filePath: String): Pointer {
        val modelRef = PointerByReference()
        lib.LiteRtCreateModelFromFile(filePath, modelRef)
        return modelRef.value
    }

    fun createOptions(): Pointer {
        val optionsRef = PointerByReference()
        lib.LiteRtCreateOptions(optionsRef)

        val options = optionsRef.value
        lib.LiteRtSetOptionsHardwareAccelerators(
            options = options,
            hardwareAccelerators = LITERT_ACCELERATOR_CPU
        )
        return options
    }

    fun createCompiledModel(
        environment: Pointer,
        model: Pointer,
        options: Pointer
    ): Pointer {
        val compiledRef = PointerByReference()
        lib.LiteRtCreateCompiledModel(
            environment = environment,
            model = model,
            compilationOptions = options,
            compiledModel = compiledRef
        )
        return compiledRef.value
    }

    fun destroy(
        environment: Pointer,
        model: Pointer,
        options: Pointer,
        compiledModel: Pointer
    ) {
        lib.LiteRtDestroyCompiledModel(compiledModel)
        lib.LiteRtDestroyOptions(options)
        lib.LiteRtDestroyModel(model)
        lib.LiteRtDestroyEnvironment(environment)
    }

    fun getInputBufferRequirements(
        compiledModel: Pointer,
        signatureIndex: Int,
        inputIndex: Int
    ): Pointer {
        val inputBufferRequirementsRef = PointerByReference()
        lib.LiteRtGetCompiledModelInputBufferRequirements(
            compiledModel = compiledModel,
            signatureIndex = signatureIndex,
            inputIndex = inputIndex,
            bufferRequirements = inputBufferRequirementsRef
        )
        return inputBufferRequirementsRef.value
    }
}