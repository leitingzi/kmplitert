package io.github.kmplitert.tool.ocr

import io.github.kmplitert.core.LiteRTCompiler
import io.github.kmplitert.tool.LiteRTHandler
import io.github.kmplitert.tool.LiteRtImage

/**
 * A high-level API for Text Recognition (OCR).
 *
 * This class coordinates the OCR process by delegating model-specific logic
 * to a [LiteRTHandler].
 *
 * @param compiler The [LiteRTCompiler] instance to use for inference.
 * @param handler The [LiteRTHandler] that handles OCR logic.
 */
class TextRecognizer(
    private val compiler: LiteRTCompiler,
    private val handler: LiteRTHandler<LiteRtImage, TextRecognitionResult>
) {

    /**
     * Recognizes text in the given [LiteRtImage].
     *
     * @param image The input image to process.
     * @return A [TextRecognitionResult] containing the recognized text and its structure.
     */
    suspend fun recognize(image: LiteRtImage): TextRecognitionResult {
        val inputBuffers = compiler.getInputBuffers()
        
        // 1. Preprocess
        handler.preprocess(image, compiler, inputBuffers)

        // 2. Inference
        val outputBuffers = compiler.getOutputBuffers()
        compiler.run(inputBuffers, outputBuffers)

        // 3. Postprocess
        return handler.postprocess(outputBuffers, compiler)
    }

    /**
     * Closes the underlying [LiteRTCompiler].
     */
    suspend fun close() {
        compiler.close()
    }
}
