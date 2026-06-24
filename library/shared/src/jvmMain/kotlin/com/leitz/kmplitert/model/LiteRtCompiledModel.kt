@file:Suppress("LocalVariableName")

package com.leitz.kmplitert.model

import com.leitz.kmplitert.LITERT_ELEMENT_TYPE_FLOAT32
import com.leitz.kmplitert.newApi.LiteRtLibrary
import com.sun.jna.Memory
import com.sun.jna.Pointer
import com.sun.jna.PointerType
import com.sun.jna.ptr.PointerByReference

class LiteRtCompiledModel : PointerType() {

    fun destroy() {
        LiteRtLibrary.INSTANCE.LiteRtDestroyCompiledModel(this)
    }

    fun getInputBufferRequirements(
        signature_index: Long,
        input_index: Long
    ): LiteRtTensorBufferRequirements {
        val ref = PointerByReference()
        val status = LiteRtLibrary.INSTANCE.LiteRtGetCompiledModelInputBufferRequirements(
            compiled_model = this,
            signature_index = signature_index,
            input_index = input_index,
            buffer_requirements = ref
        )
        check(status == 0) { "Failed to get input buffer requirements: $status" }

        val liteRtTensorBufferRequirements = LiteRtTensorBufferRequirements()
        liteRtTensorBufferRequirements.pointer = ref.value
        return liteRtTensorBufferRequirements
    }

    fun getOutputBufferRequirements(
        signature_index: Long,
        output_index: Long
    ): LiteRtTensorBufferRequirements {
        val ref = PointerByReference()
        val status = LiteRtLibrary.INSTANCE.LiteRtGetCompiledModelOutputBufferRequirements(
            compiled_model = this,
            signature_index = signature_index,
            output_index = output_index,
            buffer_requirements = ref
        )
        check(status == 0) { "Failed to get output buffer requirements: $status" }
        val liteRtTensorBufferRequirements = LiteRtTensorBufferRequirements()
        liteRtTensorBufferRequirements.pointer = ref.value
        return liteRtTensorBufferRequirements
    }

    fun getInputTensorLayout(
        signature_index: Long,
        input_index: Long
    ): LiteRtLayout {
        val layout = LiteRtLayout()
        layout.write()
        val status = LiteRtLibrary.INSTANCE.LiteRtGetCompiledModelInputTensorLayout(
            compiled_model = this,
            signature_index = signature_index,
            input_index = input_index,
            layout = layout.pointer
        )
        check(status == 0) { "Failed to get input tensor layout: $status" }
        layout.read()
        return layout
    }

    fun getOutputTensorLayout(
        signature_index: Long,
        num_layouts: Long,
        update_allocation: Boolean = true
    ): LiteRtLayout {
        val layout = LiteRtLayout()
        layout.write()

        val status = LiteRtLibrary.INSTANCE.LiteRtGetCompiledModelOutputTensorLayouts(
            compiled_model = this,
            signature_index = signature_index,
            num_layouts = num_layouts,
            layouts = layout.pointer,
            update_allocation = update_allocation
        )
        check(status == 0) { "Failed to get output tensor layout: $status" }
        layout.read()
        return layout
    }

    // TODO 需要移动到其他地方 比如TensorBuffer
    fun createManagedTensorBufferFromRequirements(
        env: LiteRtEnvironment,
        tensor_type: LiteRtRankedTensorType,
        requirements: LiteRtTensorBufferRequirements,
    ): LiteRtTensorBuffer {
        val ref = PointerByReference()
        val status = LiteRtLibrary.INSTANCE.LiteRtCreateManagedTensorBufferFromRequirements(
            env = env,
            tensor_type = tensor_type,
            requirements = requirements,
            buffer = ref
        )
        check(status == 0) {
            "Failed to create managed tensor buffer from requirements: $status"
        }

        val liteRtTensorBuffer = LiteRtTensorBuffer()
        liteRtTensorBuffer.pointer = ref.value
        return liteRtTensorBuffer
    }

    fun run(
        signature_index: Long,
        num_input_buffers: Long,
        input_buffers: LiteRtTensorBuffer,
        num_output_buffers: Long,
        output_buffers: LiteRtTensorBuffer
    ) {
        val status = LiteRtLibrary.INSTANCE.LiteRtRunCompiledModel(
            compiled_model = this,
            signature_index = signature_index,
            num_input_buffers = num_input_buffers,
            input_buffers = arrayOf(input_buffers),
            num_output_buffers = num_output_buffers,
            output_buffers = arrayOf(output_buffers),
        )
        check(status == 0) { "Failed to run model: $status" }
    }

    companion object {
        fun create(filePath: String): LiteRtCompiledModel {
            val env = LiteRtEnvironment.create()
            val model = LiteRtModel.create(filePath = filePath)
            val options = LiteRtOptions.create()
            options.setAccelerators(LiteRtHwAcceleratorSet.CPU)

            val ref = PointerByReference()
            val status = LiteRtLibrary.INSTANCE.LiteRtCreateCompiledModel(
                environment = env,
                model = model,
                compilation_options = options,
                compiled_model = ref
            )
            check(status == 0) { "Failed to create compiled model: $status" }

            val compiledModel = LiteRtCompiledModel()
            compiledModel.pointer = ref.value
            return compiledModel
        }

        fun create(env: LiteRtEnvironment, filePath: String): LiteRtCompiledModel {
            val model = LiteRtModel.create(filePath = filePath)
            val options = LiteRtOptions.create()
            options.setAccelerators(LiteRtHwAcceleratorSet.CPU)

            val ref = PointerByReference()
            val status = LiteRtLibrary.INSTANCE.LiteRtCreateCompiledModel(
                environment = env,
                model = model,
                compilation_options = options,
                compiled_model = ref
            )
            check(status == 0) { "Failed to create compiled model: $status" }

            val compiledModel = LiteRtCompiledModel()
            compiledModel.pointer = ref.value
            return compiledModel
        }
    }
}