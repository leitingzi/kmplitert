package io.github.kmplitert.tool.segmentation

import io.github.kmplitert.core.LiteRTAccelerator
import io.github.kmplitert.core.LiteRTCompiler
import io.github.kmplitert.core.LiteRTElementType
import io.github.kmplitert.tool.LiteRtImage
import io.github.kmplitert.tool.expand.getInputBuffer

/**
 * A high-level API for image segmentation.
 *
 * @param compiler The [LiteRTCompiler] instance to use for inference.
 * @param options Configuration options for segmentation.
 */
class ImageSegmenter(
    private val compiler: LiteRTCompiler,
    private val options: ImageSegmenterOptions = ImageSegmenterOptions()
) {

    /**
     * Performs segmentation on the given [LiteRtImage].
     *
     * @param image The input image to process.
     * @return An [ImageSegmenterResult] containing the segmentation mask.
     */
    suspend fun segment(image: LiteRtImage): ImageSegmenterResult {
        // 1. Preprocess
        val inputType = compiler.getInputTensorType("0")
        val layout = inputType.layout ?: throw IllegalStateException("Input layout not found")
        val inputHeight = layout.dimensions[1]
        val inputWidth = layout.dimensions[2]

        val resizedImage = if (image.width != inputWidth || image.height != inputHeight) {
            image.resize(inputWidth, inputHeight)
        } else {
            image
        }

        val inputBuffer = compiler.getInputBuffer(0)
        when (inputType.elementType) {
            LiteRTElementType.FLOAT -> {
                val floatData = resizedImage.toFloatArray(options.mean, options.std)
                inputBuffer.writeFloat(floatData)
            }
            LiteRTElementType.INT8 -> {
                val byteData = resizedImage.toInt8Array()
                inputBuffer.writeInt8(byteData)
            }
            else -> throw UnsupportedOperationException("Unsupported input element type: ${inputType.elementType}")
        }

        // 2. Inference
        val inputBuffers = compiler.getInputBuffers()
        val outputBuffers = compiler.getOutputBuffers()
        compiler.run(inputBuffers, outputBuffers)

        // 3. Postprocess
        // Common segmentation model output format: [1, height, width, num_classes] or [1, height, width]
        val outputType = compiler.getOutputTensorType("0")
        val outputLayout = outputType.layout ?: throw IllegalStateException("Output layout not found")
        val outputHeight = outputLayout.dimensions[1]
        val outputWidth = outputLayout.dimensions[2]
        
        val maskData = outputBuffers[0].readFloat()
        
        return ImageSegmenterResult(
            mask = SegmentationMask(
                width = outputWidth,
                height = outputHeight,
                data = maskData
            )
        )
    }

    /**
     * Closes the underlying [LiteRTCompiler].
     */
    suspend fun close() {
        compiler.close()
    }

    companion object {
        /**
         * Creates an [ImageSegmenter] from a model file.
         */
        suspend fun create(
            modelPath: String,
            accelerator: LiteRTAccelerator = LiteRTAccelerator.CPU,
            options: ImageSegmenterOptions = ImageSegmenterOptions()
        ): ImageSegmenter {
            val compiler = LiteRTCompiler(modelPath, accelerator)
            compiler.init()
            return ImageSegmenter(compiler, options)
        }
    }
}
