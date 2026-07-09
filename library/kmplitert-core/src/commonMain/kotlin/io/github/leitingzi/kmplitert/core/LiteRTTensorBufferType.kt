package io.github.leitingzi.kmplitert.core

/**
 * Represents the underlying storage type used by a tensor buffer.
 *
 * Different buffer types correspond to different hardware backends or
 * memory representations, allowing LiteRT to perform inference with
 * minimal data copying.
 */
enum class LiteRTTensorBufferType(val type: Int) {

    /** Unknown or unspecified buffer type. */
    Unknown(0),

    /** CPU host memory. */
    HostMemory(1),

    /** Android Hardware Buffer (AHardwareBuffer). */
    Ahwb(2),

    /** Android ION memory buffer. */
    Ion(3),

    /** Linux DMA-BUF buffer. */
    DmaBuf(4),

    /** Qualcomm FastRPC shared buffer. */
    FastRpc(5),

    /** OpenGL buffer object. */
    GlBuffer(6),

    /** OpenGL texture. */
    GlTexture(7),

    // ------------------------------------------------------------------------
    // OpenCL (10-19)
    // ------------------------------------------------------------------------

    /** OpenCL buffer. */
    OpenClBuffer(10),

    /** OpenCL FP16 buffer. */
    OpenClBufferFp16(11),

    /** OpenCL texture. */
    OpenClTexture(12),

    /** OpenCL FP16 texture. */
    OpenClTextureFp16(13),

    /** Packed OpenCL buffer. */
    OpenClBufferPacked(14),

    /** OpenCL image buffer. */
    OpenClImageBuffer(15),

    /** OpenCL FP16 image buffer. */
    OpenClImageBufferFp16(16),

    // ------------------------------------------------------------------------
    // WebGPU (20-29)
    // ------------------------------------------------------------------------

    /** WebGPU buffer. */
    WebGpuBuffer(20),

    /** WebGPU FP16 buffer. */
    WebGpuBufferFp16(21),

    /** WebGPU texture. */
    WebGpuTexture(22),

    /** WebGPU FP16 texture. */
    WebGpuTextureFp16(23),

    /** WebGPU image buffer. */
    WebGpuImageBuffer(24),

    /** WebGPU FP16 image buffer. */
    WebGpuImageBufferFp16(25),

    /** Packed WebGPU buffer. */
    WebGpuBufferPacked(26),

    // ------------------------------------------------------------------------
    // Metal (30-39)
    // ------------------------------------------------------------------------

    /** Metal buffer. */
    MetalBuffer(30),

    /** Metal FP16 buffer. */
    MetalBufferFp16(31),

    /** Metal texture. */
    MetalTexture(32),

    /** Metal FP16 texture. */
    MetalTextureFp16(33),

    /** Packed Metal buffer. */
    MetalBufferPacked(34),

    // ------------------------------------------------------------------------
    // Vulkan (40-49)
    // ------------------------------------------------------------------------

    /** Vulkan buffer. */
    VulkanBuffer(40),

    /** Vulkan FP16 buffer. */
    VulkanBufferFp16(41),

    /** Vulkan texture. */
    VulkanTexture(42),

    /** Vulkan FP16 texture. */
    VulkanTextureFp16(43),

    /** Vulkan image buffer. */
    VulkanImageBuffer(44),

    /** Vulkan FP16 image buffer. */
    VulkanImageBufferFp16(45),

    /** Packed Vulkan buffer. */
    VulkanBufferPacked(46),

    // ------------------------------------------------------------------------
    // User-defined (100-199)
    // ------------------------------------------------------------------------

    /**
     * User-defined tensor buffer.
     *
     * Custom buffer implementations must provide CPU access through
     * lock() and unlock().
     */
    UserCustomBuffer(100),

    /**
     * OpenVINO tensor buffer.
     *
     * This value is an alias of [UserCustomBuffer] and shares the same
     * native value (100).
     */
    OpenVINOTensorBuffer(100),

    /**
     * End of the reserved range for user-defined tensor buffers.
     */
    UserCustomBufferEnd(199);

    companion object {

        private val TYPE_MAP = entries.associateBy(LiteRTTensorBufferType::type)

        /**
         * Returns the corresponding buffer type for the given native value.
         *
         * @param type Native LiteRT tensor buffer type value.
         * @return Matching [LiteRTTensorBufferType], or [Unknown] if the value is not recognized.
         */
        fun from(type: Int): LiteRTTensorBufferType = TYPE_MAP[type] ?: Unknown
    }
}