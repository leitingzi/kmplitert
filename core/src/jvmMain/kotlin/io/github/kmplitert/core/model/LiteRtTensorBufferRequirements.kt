package io.github.kmplitert.core.model

import com.sun.jna.PointerType
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.LongByReference
import com.sun.jna.ptr.PointerByReference
import io.github.kmplitert.core.LiteRTBufferRequirements
import io.github.kmplitert.core.LiteRtLibrary

class LiteRtTensorBufferRequirements : PointerType() {

    fun toPlatform(): LiteRTBufferRequirements {
        val numTypesRef = IntByReference()
        LiteRtLibrary.INSTANCE.LiteRtGetNumTensorBufferRequirementsSupportedBufferTypes(this, numTypesRef.pointer)
        val numTypes = numTypesRef.value

        val supportedTypes = mutableListOf<LiteRtTensorBufferType>()
        for (i in 0 until numTypes) {
            val typeRef = IntByReference()
            LiteRtLibrary.INSTANCE.LiteRtGetTensorBufferRequirementsSupportedTensorBufferType(
                this,
                i,
                typeRef.pointer
            )
            LiteRtTensorBufferType.fromValue(typeRef.value)?.let {
                supportedTypes.add(it)
            }
        }

        val bufferSizeRef = LongByReference()
        LiteRtLibrary.INSTANCE.LiteRtGetTensorBufferRequirementsBufferSize(this, bufferSizeRef.pointer)
        val bufferSize = bufferSizeRef.value.toInt()

        val numStridesRef = IntByReference()
        val stridesPtrRef = PointerByReference()
        LiteRtLibrary.INSTANCE.LiteRtGetTensorBufferRequirementsStrides(
            this,
            numStridesRef.pointer,
            stridesPtrRef
        )
        val numStrides = numStridesRef.value
        val strides = mutableListOf<Int>()
        if (numStrides > 0 && stridesPtrRef.value != null) {
            val stridesArray = stridesPtrRef.value.getIntArray(0, numStrides)
            strides.addAll(stridesArray.toList())
        }

        return LiteRTBufferRequirements(
            supportedTypes = supportedTypes.map { it.toPlatform() },
            bufferSize = bufferSize,
            strides = strides
        )
    }

    private fun LiteRtTensorBufferType.toPlatform(): io.github.kmplitert.core.LiteRTTensorBufferType {
        return when (this) {
            LiteRtTensorBufferType.UNKNOWN -> io.github.kmplitert.core.LiteRTTensorBufferType.Unknown
            LiteRtTensorBufferType.HOST_MEMORY -> io.github.kmplitert.core.LiteRTTensorBufferType.HostMemory
            LiteRtTensorBufferType.AHWB -> io.github.kmplitert.core.LiteRTTensorBufferType.Ahwb
            LiteRtTensorBufferType.ION -> io.github.kmplitert.core.LiteRTTensorBufferType.Ion
            LiteRtTensorBufferType.DMA_BUF -> io.github.kmplitert.core.LiteRTTensorBufferType.DmaBuf
            LiteRtTensorBufferType.FAST_RPC -> io.github.kmplitert.core.LiteRTTensorBufferType.FastRpc
            LiteRtTensorBufferType.GL_BUFFER -> io.github.kmplitert.core.LiteRTTensorBufferType.GlBuffer
            LiteRtTensorBufferType.GL_TEXTURE -> io.github.kmplitert.core.LiteRTTensorBufferType.GlTexture
            LiteRtTensorBufferType.OPENCL_BUFFER -> io.github.kmplitert.core.LiteRTTensorBufferType.OpenClBuffer
            LiteRtTensorBufferType.OPENCL_BUFFER_FP16 -> io.github.kmplitert.core.LiteRTTensorBufferType.OpenClBufferFp16
            LiteRtTensorBufferType.OPENCL_TEXTURE -> io.github.kmplitert.core.LiteRTTensorBufferType.OpenClTexture
            LiteRtTensorBufferType.OPENCL_TEXTURE_FP16 -> io.github.kmplitert.core.LiteRTTensorBufferType.OpenClTextureFp16
            LiteRtTensorBufferType.OPENCL_BUFFER_PACKED -> io.github.kmplitert.core.LiteRTTensorBufferType.OpenClBufferPacked
            LiteRtTensorBufferType.OPENCL_IMAGE_BUFFER -> io.github.kmplitert.core.LiteRTTensorBufferType.OpenClImageBuffer
            LiteRtTensorBufferType.OPENCL_IMAGE_BUFFER_FP16 -> io.github.kmplitert.core.LiteRTTensorBufferType.OpenClImageBufferFp16
            LiteRtTensorBufferType.WEBGPU_BUFFER -> io.github.kmplitert.core.LiteRTTensorBufferType.WebGpuBuffer
            LiteRtTensorBufferType.WEBGPU_BUFFER_FP16 -> io.github.kmplitert.core.LiteRTTensorBufferType.WebGpuBufferFp16
            LiteRtTensorBufferType.WEBGPU_TEXTURE -> io.github.kmplitert.core.LiteRTTensorBufferType.WebGpuTexture
            LiteRtTensorBufferType.WEBGPU_TEXTURE_FP16 -> io.github.kmplitert.core.LiteRTTensorBufferType.WebGpuTextureFp16
            LiteRtTensorBufferType.WEBGPU_IMAGE_BUFFER -> io.github.kmplitert.core.LiteRTTensorBufferType.WebGpuImageBuffer
            LiteRtTensorBufferType.WEBGPU_IMAGE_BUFFER_FP16 -> io.github.kmplitert.core.LiteRTTensorBufferType.WebGpuImageBufferFp16
            LiteRtTensorBufferType.WEBGPU_BUFFER_PACKED -> io.github.kmplitert.core.LiteRTTensorBufferType.WebGpuBufferPacked
            LiteRtTensorBufferType.METAL_BUFFER -> io.github.kmplitert.core.LiteRTTensorBufferType.Unknown // Mapping to unknown if no direct match or if not applicable
            LiteRtTensorBufferType.METAL_BUFFER_FP16 -> io.github.kmplitert.core.LiteRTTensorBufferType.Unknown
            LiteRtTensorBufferType.METAL_TEXTURE -> io.github.kmplitert.core.LiteRTTensorBufferType.Unknown
            LiteRtTensorBufferType.METAL_TEXTURE_FP16 -> io.github.kmplitert.core.LiteRTTensorBufferType.Unknown
            LiteRtTensorBufferType.METAL_BUFFER_PACKED -> io.github.kmplitert.core.LiteRTTensorBufferType.Unknown
            LiteRtTensorBufferType.VULKAN_BUFFER -> io.github.kmplitert.core.LiteRTTensorBufferType.VulkanBuffer
            LiteRtTensorBufferType.VULKAN_BUFFER_FP16 -> io.github.kmplitert.core.LiteRTTensorBufferType.VulkanBufferFp16
            LiteRtTensorBufferType.VULKAN_TEXTURE -> io.github.kmplitert.core.LiteRTTensorBufferType.VulkanTexture
            LiteRtTensorBufferType.VULKAN_TEXTURE_FP16 -> io.github.kmplitert.core.LiteRTTensorBufferType.VulkanTextureFp16
            LiteRtTensorBufferType.VULKAN_IMAGE_BUFFER -> io.github.kmplitert.core.LiteRTTensorBufferType.VulkanImageBuffer
            LiteRtTensorBufferType.VULKAN_IMAGE_BUFFER_FP16 -> io.github.kmplitert.core.LiteRTTensorBufferType.VulkanImageBufferFp16
            LiteRtTensorBufferType.VULKAN_BUFFER_PACKED -> io.github.kmplitert.core.LiteRTTensorBufferType.VulkanBufferPacked
            LiteRtTensorBufferType.USER_CUSTOM_BUFFER -> io.github.kmplitert.core.LiteRTTensorBufferType.Unknown
            LiteRtTensorBufferType.OPENVINO_TENSOR_BUFFER -> io.github.kmplitert.core.LiteRTTensorBufferType.Unknown
        }
    }
}

