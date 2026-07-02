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

        init {
            val os = Platform.RESOURCE_PREFIX
            val ext = if (Platform.isWindows()) "dll" else if (Platform.isMac()) "dylib" else "so"
            val webGpuLibName = "libLiteRtWebGpuAccelerator.$ext"
            
            val webGpuLib = try {
                Native.extractFromResourcePath("$os/$webGpuLibName")
            } catch (e: Exception) {
                try {
                    Native.extractFromResourcePath(webGpuLibName)
                } catch (e2: Exception) {
                    throw RuntimeException("Failed to extract $webGpuLibName from resources (tried $os/$webGpuLibName and $webGpuLibName)", e2)
                }
            }
            
            nativeLibDir = webGpuLib.parentFile.absolutePath

            // Load dependencies and GPU accelerator to prime the process
            if (Platform.isWindows()) {
                try {
                    // Ensure dependencies are in the same directory as the plugin
                    val dxcompiler = File(nativeLibDir, "dxcompiler.dll")
                    if (!dxcompiler.exists()) {
                        val extracted = try {
                            Native.extractFromResourcePath("$os/dxcompiler.dll")
                        } catch (e: Exception) {
                            Native.extractFromResourcePath("dxcompiler.dll")
                        }
                        extracted.copyTo(dxcompiler, overwrite = true)
                    }
                    val dxil = File(nativeLibDir, "dxil.dll")
                    if (!dxil.exists()) {
                        val extracted = try {
                            Native.extractFromResourcePath("$os/dxil.dll")
                        } catch (e: Exception) {
                            Native.extractFromResourcePath("dxil.dll")
                        }
                        extracted.copyTo(dxil, overwrite = true)
                    }
                } catch (e: Exception) {
                    System.err.println("Warning: Failed to extract some Windows libraries: ${e.message}")
                }
            }

            try {
                NativeLibrary.getInstance(webGpuLib.absolutePath)
            } catch (e: UnsatisfiedLinkError) {
                // Silently ignore, the runtime will try again and report if needed
            }

            // Load main library. We try both "LiteRt" and "libLiteRt" because of non-standard naming in resources
            INSTANCE = try {
                Native.load("LiteRt", iClass)
            } catch (e: UnsatisfiedLinkError) {
                try {
                    Native.load("libLiteRt", iClass)
                } catch (e2: UnsatisfiedLinkError) {
                    // Final attempt: extract it and load by path
                    val mainLibName = "libLiteRt.$ext"
                    val extractedMain = try {
                        Native.extractFromResourcePath("$os/$mainLibName")
                    } catch (e3: Exception) {
                        Native.extractFromResourcePath(mainLibName)
                    }
                    Native.load(extractedMain.absolutePath, iClass)
                }
            }
        }
    }
}

