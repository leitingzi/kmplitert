package io.github.kmplitert.tool.face

import io.github.kmplitert.core.LiteRTAccelerator
import io.github.kmplitert.core.LiteRTCompiler
import io.github.kmplitert.core.LiteRTElementType
import io.github.kmplitert.tool.LiteRtImage
import io.github.kmplitert.tool.detection.BoundingBox
import io.github.kmplitert.tool.expand.getInputBuffer
import io.github.kmplitert.tool.expand.getOutputBuffer

/**
 * A high-level API for face analysis, including landmarks, orientation, and expressions.
 *
 * @param compiler The [LiteRTCompiler] instance to use for inference.
 * @param options Configuration options for analysis.
 */
class FaceAnalyzer(
    private val compiler: LiteRTCompiler,
    private val options: FaceAnalyzerOptions = FaceAnalyzerOptions()
) {

    /**
     * Analyzes the face in the given [LiteRtImage].
     *
     * @param image The input image to process.
     * @return A [FaceAnalysisResult] containing the analysis data.
     */
    suspend fun analyze(image: LiteRtImage): FaceAnalysisResult {
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

        // 3. Postprocess (Assumes a generic face mesh model output structure)
        // Output 0: Landmarks [1, N, 3]
        // Output 1: Scores/Expressions [1, M]
        // Output 2: Face Flag/Presence [1, 1]
        
        val landmarkRaw = outputBuffers[0].readFloat()
        val landmarks = mutableListOf<FaceLandmark>()
        for (i in 0 until landmarkRaw.size / 3) {
            landmarks.add(
                FaceLandmark(
                    x = landmarkRaw[i * 3],
                    y = landmarkRaw[i * 3 + 1],
                    z = landmarkRaw[i * 3 + 2]
                )
            )
        }

        // Calculate a basic bounding box from landmarks if not explicitly provided
        val boundingBox = if (landmarks.isNotEmpty()) {
            val minX = landmarks.minOf { it.x } * image.width
            val minY = landmarks.minOf { it.y } * image.height
            val maxX = landmarks.maxOf { it.x } * image.width
            val maxY = landmarks.maxOf { it.y } * image.height
            BoundingBox(minX, minY, maxX, maxY)
        } else null

        val expressionsRaw = outputBuffers.getOrNull(1)?.readFloat() ?: floatArrayOf()
        val expressions = expressionsRaw.indices.map { i ->
            val label = options.expressionLabels?.getOrNull(i) ?: i.toString()
            FaceExpression(label, expressionsRaw[i])
        }.filter { it.score >= options.scoreThreshold }
        .sortedByDescending { it.score }

        // orientation estimation (placeholder logic as it often requires specific model outputs or PnP solver)
        val orientation = if (landmarks.size >= 3) {
            // Simplified orientation estimation based on eye/nose position landmarks
            // This is a placeholder for actual model-based pose estimation
            FaceOrientation(0f, 0f, 0f) 
        } else null

        return FaceAnalysisResult(
            boundingBox = boundingBox,
            landmarks = landmarks,
            orientation = orientation,
            expressions = expressions
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
         * Creates a [FaceAnalyzer] from a model file.
         */
        suspend fun create(
            modelPath: String,
            accelerator: LiteRTAccelerator = LiteRTAccelerator.CPU,
            options: FaceAnalyzerOptions = FaceAnalyzerOptions()
        ): FaceAnalyzer {
            val compiler = LiteRTCompiler(modelPath, accelerator)
            compiler.init()
            return FaceAnalyzer(compiler, options)
        }
    }
}
