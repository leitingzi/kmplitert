package io.github.kmplitert.core.model

import com.sun.jna.PointerType
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.PointerByReference
import io.github.kmplitert.core.LiteRTBufferRequirements
import io.github.kmplitert.core.LiteRtLibrary
import io.github.kmplitert.core.SizeTByReference

class LiteRtTensorBufferRequirements : PointerType() {

    fun toPlatform(): LiteRTBufferRequirements {
        val numTypesRef = IntByReference()
        val numTypesStatus = LiteRtLibrary.INSTANCE.LiteRtGetNumTensorBufferRequirementsSupportedBufferTypes(
            this,
            numTypesRef
        )
        check(numTypesStatus == 0) {
            "Failed to get supported tensor buffer type count: $numTypesStatus"
        }
        val numTypes = numTypesRef.value
        require(numTypes >= 0) { "Invalid number of supported tensor buffer types: $numTypes" }

        val supportedTypes = mutableListOf<LiteRtTensorBufferType>()
        for (i in 0 until numTypes) {
            val typeRef = IntByReference()
            val typeStatus = LiteRtLibrary.INSTANCE.LiteRtGetTensorBufferRequirementsSupportedTensorBufferType(
                this,
                i,
                typeRef
            )
            check(typeStatus == 0) {
                "Failed to get supported tensor buffer type at index $i: $typeStatus"
            }
            supportedTypes.add(
                requireNotNull(LiteRtTensorBufferType.fromValue(typeRef.value)) {
                    "Unknown LiteRT tensor buffer type: ${typeRef.value}"
                }
            )
        }

        val bufferSizeRef = SizeTByReference()
        val bufferSizeStatus = LiteRtLibrary.INSTANCE.LiteRtGetTensorBufferRequirementsBufferSize(
            this,
            bufferSizeRef
        )
        check(bufferSizeStatus == 0) {
            "Failed to get tensor buffer requirements size: $bufferSizeStatus"
        }
        val bufferSize = Math.toIntExact(bufferSizeRef.value)

        val numStridesRef = IntByReference()
        val stridesPtrRef = PointerByReference()
        val stridesStatus = LiteRtLibrary.INSTANCE.LiteRtGetTensorBufferRequirementsStrides(
            this,
            numStridesRef,
            stridesPtrRef
        )
        check(stridesStatus == 0) {
            "Failed to get tensor buffer requirements strides: $stridesStatus"
        }
        val numStrides = numStridesRef.value
        require(numStrides >= 0) { "Invalid number of tensor buffer strides: $numStrides" }
        val strides = mutableListOf<Int>()
        if (numStrides > 0) {
            val stridesPointer = requireNotNull(stridesPtrRef.value) {
                "LiteRT returned a null strides pointer for $numStrides strides"
            }
            val stridesArray = stridesPointer.getIntArray(0, numStrides)
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
            LiteRtTensorBufferType.METAL_BUFFER -> io.github.kmplitert.core.LiteRTTensorBufferType.MetalBuffer
            LiteRtTensorBufferType.METAL_BUFFER_FP16 -> io.github.kmplitert.core.LiteRTTensorBufferType.MetalBufferFp16
            LiteRtTensorBufferType.METAL_TEXTURE -> io.github.kmplitert.core.LiteRTTensorBufferType.MetalTexture
            LiteRtTensorBufferType.METAL_TEXTURE_FP16 -> io.github.kmplitert.core.LiteRTTensorBufferType.MetalTextureFp16
            LiteRtTensorBufferType.METAL_BUFFER_PACKED -> io.github.kmplitert.core.LiteRTTensorBufferType.MetalBufferPacked
            LiteRtTensorBufferType.VULKAN_BUFFER -> io.github.kmplitert.core.LiteRTTensorBufferType.VulkanBuffer
            LiteRtTensorBufferType.VULKAN_BUFFER_FP16 -> io.github.kmplitert.core.LiteRTTensorBufferType.VulkanBufferFp16
            LiteRtTensorBufferType.VULKAN_TEXTURE -> io.github.kmplitert.core.LiteRTTensorBufferType.VulkanTexture
            LiteRtTensorBufferType.VULKAN_TEXTURE_FP16 -> io.github.kmplitert.core.LiteRTTensorBufferType.VulkanTextureFp16
            LiteRtTensorBufferType.VULKAN_IMAGE_BUFFER -> io.github.kmplitert.core.LiteRTTensorBufferType.VulkanImageBuffer
            LiteRtTensorBufferType.VULKAN_IMAGE_BUFFER_FP16 -> io.github.kmplitert.core.LiteRTTensorBufferType.VulkanImageBufferFp16
            LiteRtTensorBufferType.VULKAN_BUFFER_PACKED -> io.github.kmplitert.core.LiteRTTensorBufferType.VulkanBufferPacked
            LiteRtTensorBufferType.USER_CUSTOM_BUFFER -> io.github.kmplitert.core.LiteRTTensorBufferType.UserCustomBuffer
            LiteRtTensorBufferType.OPENVINO_TENSOR_BUFFER -> io.github.kmplitert.core.LiteRTTensorBufferType.OpenVINOTensorBuffer
        }
    }
}
