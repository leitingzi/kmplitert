@file:Suppress("LocalVariableName")

package io.github.kmplitert.core.model

import com.sun.jna.PointerType
import com.sun.jna.ptr.PointerByReference
import io.github.kmplitert.core.*
import java.util.concurrent.atomic.AtomicBoolean

class LiteRtCompiledModel : PointerType() {

    lateinit var env: LiteRtEnvironment
    var model: LiteRtModel? = null
    private var options: LiteRtOptions? = null
    private val destroyed = AtomicBoolean(false)
    private val bufferLock = Any()
    private val ownedBuffers = mutableListOf<LiteRtTensorBuffer>()

    fun destroy() {
        if (!destroyed.compareAndSet(false, true)) return

        val buffers = synchronized(bufferLock) {
            ownedBuffers.toList().also { ownedBuffers.clear() }
        }
        var failure: Throwable? = null
        fun cleanup(action: () -> Unit) {
            try {
                action()
            } catch (cleanupFailure: Throwable) {
                if (failure == null) {
                    failure = cleanupFailure
                } else {
                    failure.addSuppressed(cleanupFailure)
                }
            }
        }

        buffers.forEach { buffer -> cleanup(buffer::destroy) }
        if (pointer != null) {
            cleanup {
                try {
                    LiteRtLibrary.INSTANCE.LiteRtDestroyCompiledModel(this)
                } finally {
                    pointer = null
                }
            }
        }
        cleanup {
            options?.destroy()
            options = null
        }
        cleanup {
            model?.destroy()
            model = null
        }
        cleanup {
            if (::env.isInitialized) {
                env.destroy()
            }
        }
        failure?.let { throw it }
    }

    fun getInputBufferRequirements(signature_index: Long, input_index: Long): LiteRtTensorBufferRequirements {
        ensureOpen()
        val ref = PointerByReference()
        val status = LiteRtLibrary.INSTANCE.LiteRtGetCompiledModelInputBufferRequirements(
            compiled_model = this,
            signature_index = signature_index.asSizeT(),
            input_index = input_index.asSizeT(),
            buffer_requirements = ref
        )
        checkLiteRtStatus(status, "LiteRtGetCompiledModelInputBufferRequirements")
        val liteRtTensorBufferRequirements = LiteRtTensorBufferRequirements()
        liteRtTensorBufferRequirements.pointer = ref.value
        return liteRtTensorBufferRequirements
    }

    fun getOutputBufferRequirements(signature_index: Long, output_index: Long): LiteRtTensorBufferRequirements {
        ensureOpen()
        val ref = PointerByReference()
        val status = LiteRtLibrary.INSTANCE.LiteRtGetCompiledModelOutputBufferRequirements(
            compiled_model = this,
            signature_index = signature_index.asSizeT(),
            output_index = output_index.asSizeT(),
            buffer_requirements = ref
        )
        checkLiteRtStatus(status, "LiteRtGetCompiledModelOutputBufferRequirements")
        val liteRtTensorBufferRequirements = LiteRtTensorBufferRequirements()
        liteRtTensorBufferRequirements.pointer = ref.value
        return liteRtTensorBufferRequirements
    }

    fun getInputTensorLayout(signature_index: Long, input_index: Long): LiteRtLayout {
        ensureOpen()
        val layout = LiteRtLayout()
        layout.write()
        val status = LiteRtLibrary.INSTANCE.LiteRtGetCompiledModelInputTensorLayout(
            compiled_model = this,
            signature_index = signature_index.asSizeT(),
            input_index = input_index.asSizeT(),
            layout = layout.pointer
        )
        checkLiteRtStatus(status, "LiteRtGetCompiledModelInputTensorLayout")
        layout.read()
        return layout
    }

    fun getOutputTensorLayout(
        signature_index: Long,
        num_layouts: Long,
        update_allocation: Boolean
    ): List<LiteRtLayout> {
        ensureOpen()
        require(num_layouts >= 0L && num_layouts <= Int.MAX_VALUE.toLong()) {
            "Number of output layouts is out of range: $num_layouts"
        }
        val layouts = if (num_layouts == 0L) {
            emptyArray()
        } else {
            @Suppress("UNCHECKED_CAST")
            (LiteRtLayout().toArray(num_layouts.toInt()) as Array<LiteRtLayout>).also { array ->
                array.forEach(LiteRtLayout::write)
            }
        }

        val status = LiteRtLibrary.INSTANCE.LiteRtGetCompiledModelOutputTensorLayouts(
            compiled_model = this,
            signature_index = signature_index.asSizeT(),
            num_layouts = num_layouts.asSizeT(),
            layouts = layouts.firstOrNull()?.pointer ?: com.sun.jna.Pointer.NULL,
            update_allocation = (if (update_allocation) 1 else 0).toByte()
        )
        checkLiteRtStatus(status, "LiteRtGetCompiledModelOutputTensorLayouts")
        layouts.forEach(LiteRtLayout::read)
        return layouts.toList()
    }

    fun createManagedTensorBufferFromRequirements(
        requirements: LiteRtTensorBufferRequirements,
        tensorType: LiteRtRankedTensorType,
    ): LiteRtTensorBuffer {
        ensureOpen()
        val ref = PointerByReference()
        val status = LiteRtLibrary.INSTANCE.LiteRtCreateManagedTensorBufferFromRequirements(
            env = env,
            tensor_type = tensorType,
            requirements = requirements,
            buffer = ref
        )
        checkLiteRtStatus(status, "LiteRtCreateManagedTensorBufferFromRequirements")

        val liteRtTensorBuffer = LiteRtTensorBuffer()
        liteRtTensorBuffer.pointer = ref.value
        synchronized(bufferLock) {
            if (destroyed.get()) {
                liteRtTensorBuffer.destroy()
                throw IllegalStateException("Compiled model is destroyed")
            }
            ownedBuffers.add(liteRtTensorBuffer)
        }
        return liteRtTensorBuffer
    }

    fun run(signature_index: Long, input_buffers: List<TFBuffer>, output_buffers: List<TFBuffer>) {
        ensureOpen()
        val input = Array(input_buffers.size) { index ->
            input_buffers[index].asJvmBuffer()
        }

        val output = Array(output_buffers.size) { index ->
            output_buffers[index].asJvmBuffer()
        }

        val status = LiteRtLibrary.INSTANCE.LiteRtRunCompiledModel(
            compiled_model = this,
            signature_index = signature_index.asSizeT(),
            num_input_buffers = input_buffers.size.toLong().asSizeT(),
            input_buffers = input,
            num_output_buffers = output_buffers.size.toLong().asSizeT(),
            output_buffers = output,
        )
        checkLiteRtStatus(status, "LiteRtRunCompiledModel")
    }

    fun getInputBuffers(signatureIndex: Long = 0): List<TFBuffer> {
        ensureOpen()
        val model = this.model ?: throw IllegalStateException("Model is not set")
        val signature = model.getSignature(signatureIndex)
        val numInputs = signature.getNumInputs()

        val buffers = mutableListOf<TFBuffer>()
        try {
            for (i in 0 until numInputs.toInt()) {
                val inputBufferRequirements = getInputBufferRequirements(
                    signature_index = signatureIndex,
                    input_index = i.toLong()
                )

                val tensor = signature.getInputTensor(i.toLong())
                val tensorType = tensor.getRankedTensorType()
                val inputTensorBuffer = createManagedTensorBufferFromRequirements(
                    requirements = inputBufferRequirements,
                    tensorType = tensorType
                )
                buffers.add(JvmTFBuffer(inputTensorBuffer))
            }
        } catch (failure: Throwable) {
            buffers.forEach { buffer ->
                try {
                    buffer.close()
                } catch (cleanupFailure: Throwable) {
                    failure.addSuppressed(cleanupFailure)
                }
            }
            throw failure
        }
        return buffers
    }

    fun getOutputBuffers(signatureIndex: Long = 0): List<TFBuffer> {
        ensureOpen()
        val model = this.model ?: throw IllegalStateException("Model is not set")
        val signature = model.getSignature(signatureIndex)
        val numOutputs = signature.getNumOutputs()

        val buffers = mutableListOf<TFBuffer>()
        try {
            for (i in 0 until numOutputs.toInt()) {
                val outputBufferRequirements = getOutputBufferRequirements(
                    signature_index = signatureIndex,
                    output_index = i.toLong()
                )

                val tensor = signature.getOutputTensor(i.toLong())
                val tensorType = tensor.getRankedTensorType()
                val outputTensorBuffer = createManagedTensorBufferFromRequirements(
                    requirements = outputBufferRequirements,
                    tensorType = tensorType
                )
                buffers.add(JvmTFBuffer(outputTensorBuffer))
            }
        } catch (failure: Throwable) {
            buffers.forEach { buffer ->
                try {
                    buffer.close()
                } catch (cleanupFailure: Throwable) {
                    failure.addSuppressed(cleanupFailure)
                }
            }
            throw failure
        }

        return buffers
    }

    private fun ensureOpen() {
        check(!destroyed.get()) { "Compiled model is destroyed" }
    }

    private fun TFBuffer.asJvmBuffer(): LiteRtTensorBuffer {
        val jvmBuffer = this as? JvmTFBuffer
            ?: throw IllegalArgumentException("Expected a JVM tensor buffer")
        jvmBuffer.ensureOpen()
        return jvmBuffer.buffer
    }

    companion object {
        fun create(filePath: String, accelerator: LiteRtHwAcceleratorSet): LiteRtCompiledModel {
            val selectedAccelerator = if (
                accelerator == LiteRtHwAcceleratorSet.GPU && !LiteRtLibrary.isGpuAcceleratorAvailable
            ) {
                LiteRtHwAcceleratorSet.CPU
            } else {
                accelerator
            }
            val env = if (selectedAccelerator == LiteRtHwAcceleratorSet.GPU) {
                LiteRtEnvironment.createWithPluginDir(LiteRtLibrary.nativeLibDir)
            } else {
                LiteRtEnvironment.create()
            }
            var model: LiteRtModel? = null
            var options: LiteRtOptions? = null
            try {
                val createdModel = LiteRtModel.create(environment = env, filePath = filePath)
                model = createdModel
                val createdOptions = LiteRtOptions.create()
                options = createdOptions
                createdOptions.setAccelerators(selectedAccelerator)

                if (selectedAccelerator == LiteRtHwAcceleratorSet.GPU) {
                    val gpuOptions = LiteRtGpuOptions.create()
                    try {
                        gpuOptions.setBackend(LiteRtGpuBackend.AUTOMATIC)
                        createdOptions.addGpuOptions(gpuOptions)
                    } catch (failure: Throwable) {
                        gpuOptions.destroy()
                        throw failure
                    }
                }

                val ref = PointerByReference()
                val status = LiteRtLibrary.INSTANCE.LiteRtCreateCompiledModel(
                    environment = env,
                    model = createdModel,
                    compilation_options = createdOptions,
                    compiled_model = ref
                )
                checkLiteRtStatus(status, "LiteRtCreateCompiledModel")

                val compiledModel = LiteRtCompiledModel()
                compiledModel.pointer = requireNotNull(ref.value) {
                    "LiteRtCreateCompiledModel returned a null handle"
                }
                compiledModel.env = env
                compiledModel.model = createdModel
                compiledModel.options = createdOptions
                return compiledModel
            } catch (failure: Throwable) {
                options?.let { optionsToDestroy ->
                    try {
                        optionsToDestroy.destroy()
                    } catch (cleanupFailure: Throwable) {
                        failure.addSuppressed(cleanupFailure)
                    }
                }
                model?.let { modelToDestroy ->
                    try {
                        modelToDestroy.destroy()
                    } catch (cleanupFailure: Throwable) {
                        failure.addSuppressed(cleanupFailure)
                    }
                }
                try {
                    env.destroy()
                } catch (cleanupFailure: Throwable) {
                    failure.addSuppressed(cleanupFailure)
                }
                throw failure
            }
        }
    }
}
