@file:Suppress("LocalVariableName", "FunctionName")

package com.leitz.kmplitert.newApi

import com.leitz.kmplitert.model.*
import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.ptr.PointerByReference

typealias LiteRtParamIndex = Long
typealias LiteRtStatus = Int

interface LiteRtLibrary : Library {

    fun LiteRtCreateEnvironment(
        num_options: Int,
        options: Pointer?,
        environment: PointerByReference
    ): LiteRtStatus
    fun LiteRtDestroyEnvironment(environment: LiteRtEnvironment)

    fun LiteRtCreateModelFromFile(fileName: String, model: PointerByReference): LiteRtStatus
    fun LiteRtDestroyModel(model: LiteRtModel)

    fun LiteRtCreateOptions(options: PointerByReference): LiteRtStatus
    fun LiteRtDestroyOptions(options: LiteRtOptions)

    fun LiteRtSetOptionsHardwareAccelerators(
        options: LiteRtOptions,
        hardware_accelerators: Int
    ): LiteRtStatus

    fun LiteRtCreateCompiledModel(
        environment: LiteRtEnvironment,
        model: LiteRtModel,
        compilation_options: LiteRtOptions,
        compiled_model: PointerByReference,
    ): LiteRtStatus
    fun LiteRtDestroyCompiledModel(compiled_model: LiteRtCompiledModel)

    fun LiteRtGetCompiledModelInputBufferRequirements(
        compiled_model: LiteRtCompiledModel,
        signature_index: LiteRtParamIndex,
        input_index: LiteRtParamIndex,
        buffer_requirements: PointerByReference
    ): LiteRtStatus
    fun LiteRtGetCompiledModelOutputBufferRequirements(
        compiled_model: LiteRtCompiledModel,
        signature_index: LiteRtParamIndex,
        output_index: LiteRtParamIndex,
        buffer_requirements: PointerByReference
    ): LiteRtStatus

    fun LiteRtGetCompiledModelInputTensorLayout(
        compiled_model: LiteRtCompiledModel,
        signature_index: LiteRtParamIndex,
        input_index: LiteRtParamIndex,
        layout: Pointer
    ): LiteRtStatus
    fun LiteRtGetCompiledModelOutputTensorLayouts(
        compiled_model: LiteRtCompiledModel,
        signature_index: LiteRtParamIndex,
        num_layouts: Long,
        layouts: Pointer,
        update_allocation: Boolean
    ): LiteRtStatus


    fun LiteRtCreateManagedTensorBufferFromRequirements(
        env: LiteRtEnvironment,
        tensor_type: LiteRtRankedTensorType,
        requirements: LiteRtTensorBufferRequirements,
        buffer: PointerByReference
    ): LiteRtStatus

    fun LiteRtClearTensorBuffer(buffer: LiteRtTensorBuffer): LiteRtStatus
    fun LiteRtGetModelSignature(
        model: LiteRtModel,
        signature_index: LiteRtParamIndex,
        signature: PointerByReference
    ): LiteRtStatus

    fun LiteRtGetNumSignatureInputs(
        signature: Pointer,
        num_inputs: com.sun.jna.ptr.LongByReference
    ): LiteRtStatus

    fun LiteRtGetNumSignatureOutputs(
        signature: Pointer,
        num_outputs: com.sun.jna.ptr.LongByReference
    ): LiteRtStatus

    fun LiteRtGetTensorBufferPackedSize(
        tensor_buffer: LiteRtTensorBuffer,
        size: com.sun.jna.ptr.LongByReference
    ): LiteRtStatus

    fun LiteRtDestroyTensorBuffer(buffer: LiteRtTensorBuffer)


    fun LiteRtRunCompiledModel(
        compiled_model: LiteRtCompiledModel,
        signature_index: LiteRtParamIndex,
        num_input_buffers: Long,
        input_buffers: Array<LiteRtTensorBuffer>,
        num_output_buffers: Long,
        output_buffers: Array<LiteRtTensorBuffer>
    ): LiteRtStatus


    fun LiteRtLockTensorBuffer(
        tensor_buffer: LiteRtTensorBuffer,
        host_mem_addr: PointerByReference,
        lock_mode: Int
    ): LiteRtStatus

    fun LiteRtUnlockTensorBuffer(buffer: LiteRtTensorBuffer)


    companion object {
        private val iClass = LiteRtLibrary::class.java
        val INSTANCE: LiteRtLibrary = Native.load("libLiteRt", iClass)
    }
}