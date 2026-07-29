package org.example.kmplitert.runner

import io.github.kmplitert.core.LiteRTAccelerator
import io.github.kmplitert.tool.LiteRtImage
import io.github.kmplitert.tool.detection.Detection
import io.github.kmplitert.tool.detection.ObjectDetector
import org.example.kmplitert.handler.EfficientDetHandler

class EfficientDetRunner : BaseLiteRTRunner<LiteRtImage, List<Detection>>(
    modelResourcePath = "files/efficientdet_lite0.tflite",
    accelerator = LiteRTAccelerator.CPU
) {
    private var detector: ObjectDetector? = null

    override suspend fun run(input: LiteRtImage): Result<List<Detection>> {
        return try {
            ensureInitialized()
            
            if (detector == null) {
                val handler = EfficientDetHandler(scoreThreshold = 0.4f, iouThreshold = 0.5f)
                detector = ObjectDetector(compiler!!, handler)
            }

            Result.success(detector!!.detect(input))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
