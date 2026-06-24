package com.leitz.kmplitert

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.ptr.PointerByReference

typealias LiteRtStatus = Int

interface LibLiteRt: Library {
    companion object {
        val INSTANCE: LibLiteRt = Native.load("libLiteRt", LibLiteRt::class.java)
    }

    fun LiteRtCreateEnvironment(
        numOptions: Int,
        options: Pointer?,
        environment: PointerByReference
    ): LiteRtStatus

    fun LiteRtDestroyEnvironment(environment: Pointer)


    fun LiteRtCreateModelFromFile(
        fileName: String,
        model: PointerByReference
    ): LiteRtStatus

    fun LiteRtDestroyModel(model: Pointer?)

    fun LiteRtCreateOptions(options: PointerByReference): LiteRtStatus

    fun LiteRtSetOptionsHardwareAccelerators(
        options: Pointer?,
        hardwareAccelerators: Int
    ): LiteRtStatus

    fun LiteRtDestroyOptions(options: Pointer?)

    fun LiteRtCreateCompiledModel(
        environment: Pointer?,
        model: Pointer?,
        compilationOptions: Pointer?,
        compiledModel: PointerByReference
    ): LiteRtStatus

    fun LiteRtDestroyCompiledModel(compiledModel: Pointer?)


    fun LiteRtGetCompiledModelInputBufferRequirements(
        compiledModel: Pointer,
        signatureIndex: Int,
        inputIndex: Int,
        bufferRequirements: PointerByReference
    ): LiteRtStatus

    fun LiteRtGetCompiledModelOutputBufferRequirements(
        compiledModel: Pointer,
        signatureIndex: Int,
        outputIndex: Int,
        bufferRequirements: PointerByReference
    ): LiteRtStatus

    fun LiteRtGetCompiledModelInputTensorLayout(
        compiledModel: Pointer,
        signatureIndex: Int,
        inputIndex: Int,
        layout: LiteRtLayout
    ): LiteRtStatus

    fun LiteRtGetCompiledModelOutputTensorLayouts(
        compiledModel: Pointer,
        signatureIndex: Int,
        numLayouts: Int,
        layouts: LiteRtLayout,
        updateAllocation: Boolean
    ): LiteRtStatus


    fun LiteRtCreateManagedTensorBufferFromRequirements(
        env: Pointer,
        tensorType: LiteRtRankedTensorType,
        requirements: Pointer,
        buffer: PointerByReference
    ): LiteRtStatus

    fun LiteRtDestroyTensorBuffer(buffer: Pointer?)
}