package io.github.leitingzi.kmplitert.core.model

enum class LiteRtTensorBufferType(val value: Int) {

    UNKNOWN(0),

    HOST_MEMORY(1),
    AHWB(2),
    ION(3),
    DMA_BUF(4),
    FAST_RPC(5),

    GL_BUFFER(6),
    GL_TEXTURE(7),

    OPENCL_BUFFER(10),
    OPENCL_BUFFER_FP16(11),
    OPENCL_TEXTURE(12),
    OPENCL_TEXTURE_FP16(13),
    OPENCL_BUFFER_PACKED(14),
    OPENCL_IMAGE_BUFFER(15),
    OPENCL_IMAGE_BUFFER_FP16(16),

    WEBGPU_BUFFER(20),
    WEBGPU_BUFFER_FP16(21),
    WEBGPU_TEXTURE(22),
    WEBGPU_TEXTURE_FP16(23),
    WEBGPU_IMAGE_BUFFER(24),
    WEBGPU_IMAGE_BUFFER_FP16(25),
    WEBGPU_BUFFER_PACKED(26),

    METAL_BUFFER(30),
    METAL_BUFFER_FP16(31),
    METAL_TEXTURE(32),
    METAL_TEXTURE_FP16(33),
    METAL_BUFFER_PACKED(34),

    VULKAN_BUFFER(40),
    VULKAN_BUFFER_FP16(41),
    VULKAN_TEXTURE(42),
    VULKAN_TEXTURE_FP16(43),
    VULKAN_IMAGE_BUFFER(44),
    VULKAN_IMAGE_BUFFER_FP16(45),
    VULKAN_BUFFER_PACKED(46),

    USER_CUSTOM_BUFFER(100),
    OPENVINO_TENSOR_BUFFER(100);

    companion object {
        private val lookup = entries.associateBy { it.value }

        fun fromValue(value: Int): LiteRtTensorBufferType? = lookup[value]
    }
}

