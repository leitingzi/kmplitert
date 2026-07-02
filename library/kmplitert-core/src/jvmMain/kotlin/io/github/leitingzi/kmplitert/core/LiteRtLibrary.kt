@file:Suppress("LocalVariableName", "FunctionName")

package io.github.leitingzi.kmplitert.core

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.NativeLibrary
import com.sun.jna.Platform
import com.sun.jna.Pointer
import com.sun.jna.ptr.LongByReference
import com.sun.jna.ptr.PointerByReference
import io.github.leitingzi.kmplitert.core.model.LiteRtCompiledModel
import io.github.leitingzi.kmplitert.core.model.LiteRtEnvOption
import io.github.leitingzi.kmplitert.core.model.LiteRtEnvironment
import io.github.leitingzi.kmplitert.core.model.LiteRtModel
import io.github.leitingzi.kmplitert.core.model.LiteRtOptions
import io.github.leitingzi.kmplitert.core.model.LiteRtRankedTensorType
import io.github.leitingzi.kmplitert.core.model.LiteRtSignature
import io.github.leitingzi.kmplitert.core.model.LiteRtTensor
import io.github.leitingzi.kmplitert.core.model.LiteRtTensorBuffer
import io.github.leitingzi.kmplitert.core.model.LiteRtTensorBufferRequirements
import java.io.File

typealias LiteRtParamIndex = Long
typealias LiteRtStatus = Int

interface LiteRtLibrary : Library {

    fun LiteRtCreateEnvironment(
        num_options: Int,
        options: LiteRtEnvOption?,
        environment: PointerByReference
    ): LiteRtStatus
    fun LiteRtDestroyEnvironment(environment: LiteRtEnvironment)

    fun LiteRtCreateModelFromFile(fileName: String, model: PointerByReference): LiteRtStatus
    fun LiteRtDestroyModel(model: LiteRtModel)

    fun LiteRtCreateOptions(options: PointerByReference): LiteRtStatus
    fun LiteRtDestroyOptions(options: LiteRtOptions)

    fun LiteRtAddOpaqueOptions(
        options: LiteRtOptions,
        opaque_options: Pointer
    ): LiteRtStatus

    fun LiteRtCreateOpaqueOptions(
        identifier: String,
        data: Pointer,
        size: Long,
        opaque_options: PointerByReference
    ): LiteRtStatus

    fun LiteRtDestroyOpaqueOptions(opaque_options: Pointer)

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
        tensor_type: LiteRtRankedTensorType?,
        requirements: LiteRtTensorBufferRequirements,
        buffer: PointerByReference
    ): LiteRtStatus

    fun LiteRtClearTensorBuffer(buffer: LiteRtTensorBuffer): LiteRtStatus
    fun LiteRtDestroyTensorBuffer(buffer: LiteRtTensorBuffer)

    fun LiteRtGetModelSignature(
        model: LiteRtModel,
        signature_index: LiteRtParamIndex,
        signature: PointerByReference
    ): LiteRtStatus
    fun LiteRtGetNumSignatureInputs(
        signature: LiteRtSignature,
        num_inputs: LongByReference
    ): LiteRtStatus
    fun LiteRtGetNumSignatureOutputs(
        signature: LiteRtSignature,
        num_outputs: LongByReference
    ): LiteRtStatus
    fun LiteRtGetSignatureInputTensorByIndex(
        signature: LiteRtSignature,
        input_idx: LiteRtParamIndex,
        tensor: PointerByReference
    ): LiteRtStatus
    fun LiteRtGetSignatureOutputTensorByIndex(
        signature: LiteRtSignature,
        output_idx: LiteRtParamIndex,
        tensor: PointerByReference
    ): LiteRtStatus
    fun LiteRtGetRankedTensorType(
        tensor: LiteRtTensor,
        ranked_tensor_type: LiteRtRankedTensorType
    ): LiteRtStatus
    fun LiteRtGetTensorBufferPackedSize(
        tensor_buffer: LiteRtTensorBuffer,
        size: LongByReference
    ): LiteRtStatus


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

        val INSTANCE: LiteRtLibrary
        val nativeLibDir: String

        private fun extractLibrary(os: String, fileName: String): File {
            return try {
                Native.extractFromResourcePath("$os/$fileName")
            } catch (_: Exception) {
                Native.extractFromResourcePath(fileName)
            }
        }

        private fun loadOptionalPlugin(os: String, fileName: String): File? {
            return try {
                extractLibrary(os, fileName)
            } catch (e: Exception) {
                System.err.println("LiteRT optional plugin '$fileName' not found: ${e.message}")
                null
            }
        }

        private fun loadMainLibrary(os: String, ext: String): LiteRtLibrary {
            return try {
                Native.load("LiteRt", iClass)
            } catch (_: UnsatisfiedLinkError) {
                try {
                    Native.load("libLiteRt", iClass)
                } catch (_: UnsatisfiedLinkError) {
                    val extracted = extractLibrary(os, "libLiteRt.$ext")
                    Native.load(extracted.absolutePath, iClass)
                }
            }
        }


        init {
            println("RESOURCE_PREFIX = ${Platform.RESOURCE_PREFIX}")
            println("OS = ${System.getProperty("os.name")}")
            println("ARCH = ${System.getProperty("os.arch")}")

            val os = Platform.RESOURCE_PREFIX
            val ext = when {
                Platform.isWindows() -> "dll"
                Platform.isMac() -> "dylib"
                else -> "so"
            }

            println("Trying to load main: libLiteRt.$ext")

            val pluginLibName = when {
                Platform.isMac() -> "libLiteRtMetalAccelerator.$ext"
                Platform.isWindows() -> "libLiteRtWebGpuAccelerator.$ext"
                else -> "libLiteRtWebGpuAccelerator.$ext"
            }

            println("Trying to load plugin: $pluginLibName")

            val pluginLib = loadOptionalPlugin(os, pluginLibName)

            nativeLibDir = pluginLib?.parentFile?.absolutePath ?: System.getProperty("java.io.tmpdir")

            if (Platform.isWindows()) {
                try {
                    listOf("dxcompiler.dll", "dxil.dll").forEach { name ->
                        val target = File(nativeLibDir, name)
                        if (!target.exists()) {
                            extractLibrary(os, name).copyTo(target, overwrite = true)
                        }
                    }
                } catch (e: Exception) {
                    System.err.println("Warning: Failed to extract DirectX dependencies: ${e.message}")
                }
            }

            pluginLib?.let {
                try {
                    NativeLibrary.getInstance(it.absolutePath)
                } catch (e: UnsatisfiedLinkError) {
                    System.err.println("Warning: Failed to load optional accelerator '${it.name}': ${e.message}")
                }
            }

            INSTANCE = loadMainLibrary(os, ext)
        }
    }
}

