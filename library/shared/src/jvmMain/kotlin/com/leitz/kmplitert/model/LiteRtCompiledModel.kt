@file:Suppress("LocalVariableName")

package com.leitz.kmplitert.model

import com.leitz.kmplitert.JvmTFBuffer
import com.leitz.kmplitert.LITERT_ELEMENT_TYPE_FLOAT32
import com.leitz.kmplitert.TFBuffer
import com.leitz.kmplitert.newApi.LiteRtLibrary
import com.sun.jna.Platform
import com.sun.jna.PointerType
import com.sun.jna.ptr.PointerByReference

class LiteRtCompiledModel : PointerType() {

    lateinit var env: LiteRtEnvironment
    var model: LiteRtModel? = null

    fun destroy() {
        LiteRtLibrary.INSTANCE.LiteRtDestroyCompiledModel(this)
        model?.destroy()
    }

    fun getInputBufferRequirements(signature_index: Long, input_index: Long): LiteRtTensorBufferRequirements {
        val ref = PointerByReference()
        val status = LiteRtLibrary.INSTANCE.LiteRtGetCompiledModelInputBufferRequirements(
            compiled_model = this,
            signature_index = signature_index,
            input_index = input_index,
            buffer_requirements = ref
        )
        check(status == 0) {
            "Failed to get input buffer requirements: $status"
        }
        val liteRtTensorBufferRequirements = LiteRtTensorBufferRequirements()
        liteRtTensorBufferRequirements.pointer = ref.value
        return liteRtTensorBufferRequirements
    }

    fun getOutputBufferRequirements(signature_index: Long, output_index: Long): LiteRtTensorBufferRequirements {
        val ref = PointerByReference()
        val status = LiteRtLibrary.INSTANCE.LiteRtGetCompiledModelOutputBufferRequirements(
            compiled_model = this,
            signature_index = signature_index,
            output_index = output_index,
            buffer_requirements = ref
        )
        check(status == 0) {
            "Failed to get output buffer requirements: $status"
        }
        val liteRtTensorBufferRequirements = LiteRtTensorBufferRequirements()
        liteRtTensorBufferRequirements.pointer = ref.value
        return liteRtTensorBufferRequirements
    }

    fun getInputTensorLayout(signature_index: Long, input_index: Long): LiteRtLayout {
        val layout = LiteRtLayout()
        layout.write()
        val status = LiteRtLibrary.INSTANCE.LiteRtGetCompiledModelInputTensorLayout(
            compiled_model = this,
            signature_index = signature_index,
            input_index = input_index,
            layout = layout.pointer
        )
        check(status == 0) {
            "Failed to get input tensor layout: $status"
        }
        layout.read()
        return layout
    }

    fun getOutputTensorLayout(signature_index: Long, num_layouts: Long, update_allocation: Boolean): LiteRtLayout {
        val layout = LiteRtLayout()
        layout.write()

        val status = LiteRtLibrary.INSTANCE.LiteRtGetCompiledModelOutputTensorLayouts(
            compiled_model = this,
            signature_index = signature_index,
            num_layouts = num_layouts,
            layouts = layout.pointer,
            update_allocation = update_allocation
        )
        check(status == 0) {
            "Failed to get output tensor layout: $status"
        }
        layout.read()
        return layout
    }

    fun createManagedTensorBufferFromRequirements(
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

    fun run(signature_index: Long, input_buffers: List<TFBuffer>, output_buffers: List<TFBuffer>) {
        val input = Array(input_buffers.size) { index ->
            (input_buffers[index] as JvmTFBuffer).buffer
        }

        val output = Array(output_buffers.size) { index ->
            (output_buffers[index] as JvmTFBuffer).buffer
        }

        val status = LiteRtLibrary.INSTANCE.LiteRtRunCompiledModel(
            compiled_model = this,
            signature_index = signature_index,
            num_input_buffers = input_buffers.size.toLong(),
            input_buffers = input,
            num_output_buffers = output_buffers.size.toLong(),
            output_buffers = output,
        )
        check(status == 0) {
            "Failed to run model: $status"
        }
    }

    fun getInputBuffers(signatureIndex: Long = 0): List<TFBuffer> {
        val model = this.model ?: throw IllegalStateException("Model is not set")
        val signature = model.getSignature(signatureIndex)
        val numInputs = signature.getNumInputs()

        println("numInputs = $numInputs")

        val buffers = mutableListOf<TFBuffer>()
        for (i in 0 until numInputs) {
            val inputBufferRequirements = getInputBufferRequirements(
                signature_index = signatureIndex,
                input_index = i
            )

            val inputLayout = getInputTensorLayout(signature_index = signatureIndex, input_index = i)

            val inputRankedType = LiteRtRankedTensorType()
            inputRankedType.elementType = LITERT_ELEMENT_TYPE_FLOAT32
            inputRankedType.layout.flags = inputLayout.flags
            if (Platform.isWindows()) {
                inputRankedType.layout.padding = inputLayout.padding
            }
            for (j in 0 until 8) {
                inputRankedType.layout.dimensions[j] = inputLayout.dimensions[j]
                inputRankedType.layout.strides[j] = inputLayout.strides[j]
            }
            inputRankedType.write()

            val intputTensorBuffer = createManagedTensorBufferFromRequirements(
                tensor_type = inputRankedType,
                requirements = inputBufferRequirements
            )
            buffers.add(JvmTFBuffer(intputTensorBuffer))
        }
        return buffers
    }

    fun getOutputBuffers(signatureIndex: Long = 0): List<TFBuffer> {
        val model = this.model ?: throw IllegalStateException("Model is not set")
        val signature = model.getSignature(signatureIndex)
        val numOutputs = signature.getNumOutputs()

        // Get all layouts at once
        val layouts = LiteRtLayout().toArray(numOutputs.toInt()) as Array<LiteRtLayout>
        val status = LiteRtLibrary.INSTANCE.LiteRtGetCompiledModelOutputTensorLayouts(
            compiled_model = this,
            signature_index = signatureIndex,
            num_layouts = numOutputs,
            layouts = layouts[0].pointer,
            update_allocation = false
        )
        check(status == 0) { "Failed to get output tensor layouts: $status" }

        val buffers = mutableListOf<TFBuffer>()
        for (i in 0 until numOutputs.toInt()) {
            val outputBufferRequirements = getOutputBufferRequirements(
                signature_index = signatureIndex,
                output_index = i.toLong()
            )

            val outputLayout = layouts[i]
            outputLayout.read() // MUST call read() to sync from native memory

            val outputRankedType = LiteRtRankedTensorType()
            outputRankedType.elementType = LITERT_ELEMENT_TYPE_FLOAT32
            outputRankedType.layout.flags = outputLayout.flags
            if (Platform.isWindows()) {
                outputRankedType.layout.padding = outputLayout.padding
            }
            for (j in 0 until 8) {
                outputRankedType.layout.dimensions[j] = outputLayout.dimensions[j]
                outputRankedType.layout.strides[j] = outputLayout.strides[j]
            }
            outputRankedType.write()

            val outputTensorBuffer = createManagedTensorBufferFromRequirements(
                tensor_type = outputRankedType,
                requirements = outputBufferRequirements
            )
            buffers.add(JvmTFBuffer(outputTensorBuffer))
        }

        return buffers
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
            check(status == 0) {
                "Failed to create compiled model: $status"
            }

            val compiledModel = LiteRtCompiledModel()
            compiledModel.pointer = ref.value
            compiledModel.env = env
            compiledModel.model = model
            return compiledModel
        }
    }
}