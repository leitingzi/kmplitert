package io.github.kmplitert.tool.gesture

import io.github.kmplitert.core.LiteRTAccelerator
import io.github.kmplitert.core.LiteRTCompiler
import io.github.kmplitert.core.LiteRTElementType
import io.github.kmplitert.tool.LiteRtImage
import io.github.kmplitert.tool.expand.getInputBuffer
import io.github.kmplitert.tool.expand.getOutputBuffer

/**
 * A high-level API for hand landmark detection and gesture recognition.
 *
 * This class handles image preprocessing and maps the model output tensors
 * to structured hand landmark data and recognized gestures.
 *
 * @param compiler The [LiteRTCompiler] instance to use for inference.
 * @param options Configuration options for recognition.
 */
class HandGestureRecognizer(
    private val compiler: LiteRTCompiler,
    private val options: HandGestureRecognizerOptions = HandGestureRecognizerOptions()
) {

    /**
     * Recognizes hand landmarks and gestures in the given [LiteRtImage].
     *
     * @param image The input image to process.
     * @return A [HandGestureResult] containing detected landmarks and gestures.
     */
    suspend fun recognize(image: LiteRtImage): HandGestureResult {
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
        // Model output mapping (example assumes common landmark model structure)
        // Output 0: Landmarks [1, 21, 3] or [1, 63]
        // Output 1: Handness [1, 1]
        // Output 2: Gestures [1, num_gestures]
        
        val landmarkRaw = outputBuffers[0].readFloat()
        val landmarks = mutableListOf<HandLandmark>()
        for (i in 0 until (landmarkRaw.size / 3).coerceAtMost(21)) {
            landmarks.add(
                HandLandmark(
                    x = landmarkRaw[i * 3],
                    y = landmarkRaw[i * 3 + 1],
                    z = landmarkRaw[i * 3 + 2]
                )
            )
        }

        val handnessRaw = outputBuffers.getOrNull(1)?.readFloat()?.firstOrNull() ?: 0f
        val handedness = if (handnessRaw > 0.5f) "Right" to handnessRaw else "Left" to (1f - handnessRaw)

        val gesturesRaw = outputBuffers.getOrNull(2)?.readFloat() ?: floatArrayOf()
        val gestures = gesturesRaw.indices.map { i ->
            val label = options.labels?.getOrNull(i) ?: i.toString()
            Gesture(label, gesturesRaw[i], i)
        }.filter { it.score >= options.scoreThreshold }
        .sortedByDescending { it.score }
        .let { if (options.maxResults > 0) it.take(options.maxResults) else it }

        return HandGestureResult(
            landmarks = landmarks,
            gestures = gestures,
            handedness = handedness
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
         * Creates a [HandGestureRecognizer] from a model file.
         *
         * @param modelPath Path to the LiteRT model file.
         * @param accelerator Preferred hardware accelerator. Defaults to CPU.
         * @param options Configuration options for recognition.
         * @return An initialized [HandGestureRecognizer].
         */
        suspend fun create(
            modelPath: String,
            accelerator: LiteRTAccelerator = LiteRTAccelerator.CPU,
            options: HandGestureRecognizerOptions = HandGestureRecognizerOptions()
        ): HandGestureRecognizer {
            val compiler = LiteRTCompiler(modelPath, accelerator)
            compiler.init()
            return HandGestureRecognizer(compiler, options)
        }
    }
}
