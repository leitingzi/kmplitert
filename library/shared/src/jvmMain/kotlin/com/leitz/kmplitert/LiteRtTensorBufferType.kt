package com.leitz.kmplitert

import com.sun.jna.Callback
import com.sun.jna.Pointer

object LiteRtTensorBufferType {
    const val UNKNOWN = 0
    const val HOST_MEMORY = 1
    const val AHWB = 2
    const val ION = 3
    const val DMA_BUF = 4
    const val FASTRPC = 5
    const val GL_BUFFER = 6
    const val GL_TEXTURE = 7

    // OpenCL 10-19
    const val OPENCL_BUFFER = 10
    const val OPENCL_BUFFER_FP16 = 11
    const val OPENCL_TEXTURE = 12
    const val OPENCL_TEXTURE_FP16 = 13
    const val OPENCL_BUFFER_PACKED = 14
    const val OPENCL_IMAGE_BUFFER = 15
    const val OPENCL_IMAGE_BUFFER_FP16 = 16

    // WebGPU 20-29
    const val WEBGPU_BUFFER = 20
    const val WEBGPU_BUFFER_FP16 = 21
    const val WEBGPU_TEXTURE = 22
    const val WEBGPU_TEXTURE_FP16 = 23
    const val WEBGPU_IMAGE_BUFFER = 24
    const val WEBGPU_IMAGE_BUFFER_FP16 = 25
    const val WEBGPU_BUFFER_PACKED = 26

    // Metal 30-39
    const val METAL_BUFFER = 30
    const val METAL_BUFFER_FP16 = 31
    const val METAL_TEXTURE = 32
    const val METAL_TEXTURE_FP16 = 33
    const val METAL_BUFFER_PACKED = 34

    // Vulkan 40-49
    const val VULKAN_BUFFER = 40
    const val VULKAN_BUFFER_FP16 = 41
    const val VULKAN_TEXTURE = 42
    const val VULKAN_TEXTURE_FP16 = 43
    const val VULKAN_IMAGE_BUFFER = 44
    const val VULKAN_IMAGE_BUFFER_FP16 = 45
    const val VULKAN_BUFFER_PACKED = 46

    // User custom 100-199
    const val USER_CUSTOM_BUFFER = 100
    const val OPENVINO_TENSOR_BUFFER = 100
    const val USER_CUSTOM_BUFFER_END = 199
}

fun interface LiteRtHostMemoryDeallocator : Callback {
    fun invoke(addr: Pointer?)
}

fun interface LiteRtAhwbDeallocator : Callback {
    fun invoke(ahwb: Pointer?) // AHardwareBuffer*
}

fun interface LiteRtIonDeallocator : Callback {
    fun invoke(addr: Pointer?)
}

fun interface LiteRtDmaBufDeallocator : Callback {
    fun invoke(addr: Pointer?)
}

fun interface LiteRtFastRpcDeallocator : Callback {
    fun invoke(addr: Pointer?)
}

fun interface LiteRtOpenClDeallocator : Callback {
    fun invoke(addr: Pointer?)
}

fun interface LiteRtGlBufferDeallocator : Callback {
    fun invoke(addr: Pointer?)
}

fun interface LiteRtGlTextureDeallocator : Callback {
    fun invoke(addr: Pointer?)
}

fun interface LiteRtWebGpuBufferDeallocator : Callback {
    fun invoke(addr: Pointer?)
}

fun interface LiteRtWebGpuTextureDeallocator : Callback {
    fun invoke(addr: Pointer?)
}

fun interface LiteRtMetalDeallocator : Callback {
    fun invoke(addr: Pointer?)
}

fun interface LiteRtVulkanMemoryDeallocator : Callback {
    fun invoke(addr: Pointer?)
}

object LiteRtTensorBufferUtils {

    fun isUserCustom(type: Int): Boolean =
        type in LiteRtTensorBufferType.USER_CUSTOM_BUFFER..LiteRtTensorBufferType.USER_CUSTOM_BUFFER_END

    fun isOpenClMemory(type: Int): Boolean =
        type in setOf(
            LiteRtTensorBufferType.OPENCL_BUFFER,
            LiteRtTensorBufferType.OPENCL_BUFFER_FP16,
            LiteRtTensorBufferType.OPENCL_TEXTURE,
            LiteRtTensorBufferType.OPENCL_TEXTURE_FP16,
            LiteRtTensorBufferType.OPENCL_BUFFER_PACKED,
            LiteRtTensorBufferType.OPENCL_IMAGE_BUFFER,
            LiteRtTensorBufferType.OPENCL_IMAGE_BUFFER_FP16
        )

    fun isWebGpuMemory(type: Int): Boolean =
        type in setOf(
            LiteRtTensorBufferType.WEBGPU_BUFFER,
            LiteRtTensorBufferType.WEBGPU_BUFFER_FP16,
            LiteRtTensorBufferType.WEBGPU_TEXTURE,
            LiteRtTensorBufferType.WEBGPU_TEXTURE_FP16,
            LiteRtTensorBufferType.WEBGPU_IMAGE_BUFFER,
            LiteRtTensorBufferType.WEBGPU_IMAGE_BUFFER_FP16,
            LiteRtTensorBufferType.WEBGPU_BUFFER_PACKED
        )

    fun isMetalMemory(type: Int): Boolean =
        type in setOf(
            LiteRtTensorBufferType.METAL_BUFFER,
            LiteRtTensorBufferType.METAL_BUFFER_FP16,
            LiteRtTensorBufferType.METAL_TEXTURE,
            LiteRtTensorBufferType.METAL_TEXTURE_FP16,
            LiteRtTensorBufferType.METAL_BUFFER_PACKED
        )

    fun isVulkanMemory(type: Int): Boolean =
        type in setOf(
            LiteRtTensorBufferType.VULKAN_BUFFER,
            LiteRtTensorBufferType.VULKAN_BUFFER_FP16,
            LiteRtTensorBufferType.VULKAN_TEXTURE,
            LiteRtTensorBufferType.VULKAN_TEXTURE_FP16,
            LiteRtTensorBufferType.VULKAN_IMAGE_BUFFER,
            LiteRtTensorBufferType.VULKAN_IMAGE_BUFFER_FP16,
            LiteRtTensorBufferType.VULKAN_BUFFER_PACKED
        )

    fun isGpuBuffer(type: Int): Boolean =
        type in setOf(
            LiteRtTensorBufferType.GL_BUFFER,
            LiteRtTensorBufferType.OPENCL_BUFFER,
            LiteRtTensorBufferType.OPENCL_BUFFER_FP16,
            LiteRtTensorBufferType.OPENCL_BUFFER_PACKED,
            LiteRtTensorBufferType.WEBGPU_BUFFER,
            LiteRtTensorBufferType.WEBGPU_BUFFER_FP16,
            LiteRtTensorBufferType.WEBGPU_BUFFER_PACKED,
            LiteRtTensorBufferType.METAL_BUFFER,
            LiteRtTensorBufferType.METAL_BUFFER_FP16,
            LiteRtTensorBufferType.METAL_BUFFER_PACKED,
            LiteRtTensorBufferType.VULKAN_BUFFER,
            LiteRtTensorBufferType.VULKAN_BUFFER_FP16,
            LiteRtTensorBufferType.VULKAN_BUFFER_PACKED
        )

    fun isGpuTexture(type: Int): Boolean =
        type in setOf(
            LiteRtTensorBufferType.GL_TEXTURE,
            LiteRtTensorBufferType.OPENCL_TEXTURE,
            LiteRtTensorBufferType.OPENCL_TEXTURE_FP16,
            LiteRtTensorBufferType.WEBGPU_TEXTURE,
            LiteRtTensorBufferType.WEBGPU_TEXTURE_FP16,
            LiteRtTensorBufferType.METAL_TEXTURE,
            LiteRtTensorBufferType.METAL_TEXTURE_FP16,
            LiteRtTensorBufferType.VULKAN_TEXTURE,
            LiteRtTensorBufferType.VULKAN_TEXTURE_FP16
        )

    fun isGpuImageBuffer(type: Int): Boolean =
        type in setOf(
            LiteRtTensorBufferType.OPENCL_IMAGE_BUFFER,
            LiteRtTensorBufferType.OPENCL_IMAGE_BUFFER_FP16,
            LiteRtTensorBufferType.WEBGPU_IMAGE_BUFFER,
            LiteRtTensorBufferType.WEBGPU_IMAGE_BUFFER_FP16,
            LiteRtTensorBufferType.VULKAN_IMAGE_BUFFER,
            LiteRtTensorBufferType.VULKAN_IMAGE_BUFFER_FP16
        )

    fun isGpuFp16(type: Int): Boolean =
        type in setOf(
            LiteRtTensorBufferType.OPENCL_BUFFER_FP16,
            LiteRtTensorBufferType.OPENCL_TEXTURE_FP16,
            LiteRtTensorBufferType.OPENCL_IMAGE_BUFFER_FP16,
            LiteRtTensorBufferType.WEBGPU_BUFFER_FP16,
            LiteRtTensorBufferType.WEBGPU_TEXTURE_FP16,
            LiteRtTensorBufferType.WEBGPU_IMAGE_BUFFER_FP16,
            LiteRtTensorBufferType.METAL_BUFFER_FP16,
            LiteRtTensorBufferType.METAL_TEXTURE_FP16,
            LiteRtTensorBufferType.VULKAN_BUFFER_FP16,
            LiteRtTensorBufferType.VULKAN_TEXTURE_FP16,
            LiteRtTensorBufferType.VULKAN_IMAGE_BUFFER_FP16
        )
}