package io.github.kmplitert.tool.pose

import io.github.kmplitert.core.LiteRTAccelerator
import io.github.kmplitert.core.LiteRTCompiler
import io.github.kmplitert.core.LiteRTElementType
import io.github.kmplitert.tool.LiteRtImage
import io.github.kmplitert.tool.expand.getInputBuffer

/**
 * A high-level API for skeletal tracking (Pose Estimation).
 *
 * @param compiler The [LiteRTCompiler] instance to use for inference.
 * @param options Configuration options for tracking.
 */
class PoseTracker(
    private val compiler: LiteRTCompiler,
    private val options: PoseTrackerOptions = PoseTrackerOptions()
) {

    /**
     * Tracks pose in the given [LiteRtImage].
     *
     * @param image The input image to process.
     * @return A [PoseResult] containing detected landmarks.
     */
    suspend fun track(image: LiteRtImage): PoseResult {
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
        // Common pose model output format: [1, 1, 17, 3] or [1, 17, 3] for MoveNet
        // Or [1, 33, 5] for BlazePose (x, y, z, visibility, presence)
        val outputTensor = outputBuffers[0]
        val outputData = outputTensor.readFloat()
        
        val landmarks = mutableListOf<PoseLandmark>()
        
        // Detection logic based on common output shapes
        val numLandmarks = if (outputData.size % 3 == 0) outputData.size / 3 else if (outputData.size % 5 == 0) outputData.size / 5 else 0
        val stride = if (outputData.size % 3 == 0) 3 else 5

        for (i in 0 until numLandmarks) {
            val score = if (stride == 3) outputData[i * stride + 2] else outputData[i * stride + 3]
            if (score >= options.minScoreThreshold) {
                landmarks.add(
                    PoseLandmark(
                        x = outputData[i * stride + 1], // Usually y, x, score in MoveNet
                        y = outputData[i * stride],
                        z = if (stride == 5) outputData[i * stride + 2] else 0f,
                        score = score
                    )
                )
            }
        }

        return PoseResult(landmarks)
    }

    /**
     * Closes the underlying [LiteRTCompiler].
     */
    suspend fun close() {
        compiler.close()
    }

    companion object {
        /**
         * Creates a [PoseTracker] from a model file.
         */
        suspend fun create(
            modelPath: String,
            accelerator: LiteRTAccelerator = LiteRTAccelerator.CPU,
            options: PoseTrackerOptions = PoseTrackerOptions()
        ): PoseTracker {
            val compiler = LiteRTCompiler(modelPath, accelerator)
            compiler.init()
            return PoseTracker(compiler, options)
        }
    }
}
