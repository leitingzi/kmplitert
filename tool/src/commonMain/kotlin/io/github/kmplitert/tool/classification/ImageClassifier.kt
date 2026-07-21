package io.github.kmplitert.tool.classification

import io.github.kmplitert.core.LiteRTAccelerator
import io.github.kmplitert.core.LiteRTCompiler
import io.github.kmplitert.core.LiteRTElementType
import io.github.kmplitert.tool.LiteRtImage

/**
 * A high-level API for performing image classification using LiteRT.
 *
 * This class simplifies the process of image preprocessing (resizing and normalization),
 * running inference, and decoding the output probabilities into a list of [Category] objects.
 *
 * @param compiler The [LiteRTCompiler] instance to use for inference.
 * @param options Configuration options for classification.
 */
class ImageClassifier(
    private val compiler: LiteRTCompiler,
    private val options: ImageClassifierOptions = ImageClassifierOptions()
) {

    /**
     * Classifies the given [LiteRtImage].
     *
     * This method handles resizing the image to match the model's input requirements,
     * normalizing the pixel data, running inference, and decoding the results.
     *
     * @param image The input image to classify.
     * @param inputName The name of the input tensor. Defaults to "0".
     * @param outputName The name of the output tensor. Defaults to "0".
     * @return A list of [Category] results, sorted by score descending.
     */
    suspend fun classify(
        image: LiteRtImage,
        inputName: String = "0",
        outputName: String = "0"
    ): List<Category> {
        // 1. Get Metadata
        val inputType = try {
            compiler.getInputTensorType(inputName)
        } catch (e: Exception) {
            // Fallback for models that might use index-based naming if the provided name fails
            if (inputName != "0") compiler.getInputTensorType("0") else throw e
        }
        
        val outputType = try {
            compiler.getOutputTensorType(outputName)
        } catch (e: Exception) {
            if (outputName != "0") compiler.getOutputTensorType("0") else throw e
        }

        // Get dimensions from layout
        val layout = inputType.layout ?: throw IllegalStateException("Input layout not found")
        // Typically NHWC or NCHW. We assume HW are indices 1 and 2 for NHWC or 2 and 3 for NCHW.
        val inputHeight = layout.dimensions[1]
        val inputWidth = layout.dimensions[2]

        // 2. Preprocess
        val resizedImage = if (image.width != inputWidth || image.height != inputHeight) {
            image.resize(inputWidth, inputHeight)
        } else {
            image
        }

        val inputBuffers = compiler.getInputBuffers()
        val inputBuffer = inputBuffers[0]

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

        // 3. Inference
        val outputBuffers = compiler.getOutputBuffers()
        compiler.run(inputBuffers, outputBuffers)

        // 4. Postprocess
        val scores = when (outputType.elementType) {
            LiteRTElementType.FLOAT -> outputBuffers[0].readFloat()
            LiteRTElementType.INT8 -> {
                val bytes = outputBuffers[0].readInt8()
                FloatArray(bytes.size) { (bytes[it].toInt() and 0xFF).toFloat() / 255f }
            }
            else -> throw UnsupportedOperationException("Unsupported output element type: ${outputType.elementType}")
        }

        val results = scores.mapIndexed { index, score ->
            val label = options.labels?.getOrNull(index) ?: index.toString()
            Category(label, score, index)
        }
        .filter { it.score >= options.scoreThreshold }
        .sortedByDescending { it.score }

        return if (options.topK > 0) {
            results.take(options.topK)
        } else {
            results
        }
    }

    /**
     * Closes the underlying [LiteRTCompiler].
     */
    suspend fun close() {
        compiler.close()
    }

    companion object {
        /**
         * Creates an [ImageClassifier] from a model file.
         *
         * This helper method creates and initializes a [LiteRTCompiler] automatically.
         *
         * @param modelPath Path to the LiteRT model file.
         * @param accelerator Preferred hardware accelerator. Defaults to CPU.
         * @param options Configuration options for classification.
         * @return An initialized [ImageClassifier].
         */
        suspend fun create(
            modelPath: String,
            accelerator: LiteRTAccelerator = LiteRTAccelerator.CPU,
            options: ImageClassifierOptions = ImageClassifierOptions()
        ): ImageClassifier {
            val compiler = LiteRTCompiler(modelPath, accelerator)
            compiler.init()
            return ImageClassifier(compiler, options)
        }
    }
}
