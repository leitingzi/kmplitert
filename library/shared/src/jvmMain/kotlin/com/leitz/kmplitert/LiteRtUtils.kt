package com.leitz.kmplitert

import com.sun.jna.Pointer
import com.sun.jna.ptr.PointerByReference

object LiteRtUtils {
    private val lib = LibLiteRt.INSTANCE

    fun createEnvironment(): Pointer {
        val envRef = PointerByReference()
        val status = lib.LiteRtCreateEnvironment(numOptions = 0, options = null, environment = envRef)
        if (status != 0) {
            throw RuntimeException("Failed to create environment: $status")
        }
        return envRef.value
    }

    fun createModel(filePath: String): Pointer {
        val modelRef = PointerByReference()
        val status = lib.LiteRtCreateModelFromFile(filePath, modelRef)
        if (status != 0) {
            throw RuntimeException("Failed to create model from file $filePath: $status")
        }
        return modelRef.value
    }

    fun createOptions(): Pointer {
        val optionsRef = PointerByReference()
        var status = lib.LiteRtCreateOptions(optionsRef)
        if (status != 0) {
            throw RuntimeException("Failed to create options: $status")
        }

        val options = optionsRef.value
        status = lib.LiteRtSetOptionsHardwareAccelerators(
            options = options,
            hardwareAccelerators = LITERT_ACCELERATOR_CPU
        )
        if (status != 0) {
            throw RuntimeException("Failed to set options hardware accelerators: $status")
        }
        return options
    }

    fun createCompiledModel(
        environment: Pointer,
        model: Pointer,
        options: Pointer
    ): Pointer {
        val compiledRef = PointerByReference()
        val status = lib.LiteRtCreateCompiledModel(
            environment = environment,
            model = model,
            compilationOptions = options,
            compiledModel = compiledRef
        )
        if (status != 0) {
            throw RuntimeException("Failed to create compiled model: $status")
        }
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
        val status = lib.LiteRtGetCompiledModelInputBufferRequirements(
            compiledModel = compiledModel,
            signatureIndex = signatureIndex,
            inputIndex = inputIndex,
            bufferRequirements = inputBufferRequirementsRef
        )
        if (status != 0) {
            throw RuntimeException("Failed to get input buffer requirements: $status")
        }
        return inputBufferRequirementsRef.value
    }

    fun getOutputBufferRequirements(
        compiledModel: Pointer,
        signatureIndex: Int,
        outputIndex: Int
    ): Pointer {
        val outputBufferRequirementsRef = PointerByReference()
        val status = lib.LiteRtGetCompiledModelOutputBufferRequirements(
            compiledModel = compiledModel,
            signatureIndex = signatureIndex,
            outputIndex = outputIndex,
            bufferRequirements = outputBufferRequirementsRef
        )
        if (status != 0) {
            throw RuntimeException("Failed to get output buffer requirements: $status")
        }
        return outputBufferRequirementsRef.value
    }

    fun getInputTensorLayout(
        compiledModel: Pointer,
        signatureIndex: Int,
        inputIndex: Int
    ): LiteRtLayout {
        val layout = LiteRtLayout()
        layout.write()

        val status = lib.LiteRtGetCompiledModelInputTensorLayout(
            compiledModel = compiledModel,
            signatureIndex = signatureIndex,
            inputIndex = inputIndex,
            layout = layout
        )
        if (status != 0) {
            throw RuntimeException("Failed to get input tensor layout: $status")
        }

        layout.read()
        return layout
    }

    fun getOutputTensorLayout(
        compiledModel: Pointer,
        signatureIndex: Int,
        numLayouts: Int,
        updateAllocation: Boolean
    ): LiteRtLayout {
        val layout = LiteRtLayout()
        layout.write()

        val status = lib.LiteRtGetCompiledModelOutputTensorLayouts(
            compiledModel = compiledModel,
            signatureIndex = signatureIndex,
            numLayouts = numLayouts,
            layouts = layout,
            updateAllocation = updateAllocation
        )

        if (status != 0) {
            throw RuntimeException("Failed to get output tensor layouts: $status")
        }

        layout.read()
        return layout
    }

    fun createTensorBufferFromRequirements(
        env: Pointer,
        tensorType: LiteRtRankedTensorType,
        requirements: Pointer
    ): Pointer {
        val tensorBufferRef = PointerByReference()
        val status = lib.LiteRtCreateManagedTensorBufferFromRequirements(
            env = env,
            tensorType = tensorType,
            requirements = requirements,
            buffer = tensorBufferRef
        )
        if (status != 0) {
            throw RuntimeException("Failed to create managed tensor buffer from requirements: $status")
        }
        return tensorBufferRef.value
    }
}