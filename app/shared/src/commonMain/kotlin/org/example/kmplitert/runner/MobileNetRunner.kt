package org.example.kmplitert.runner

import io.github.kmplitert.core.LiteRTAccelerator
import io.github.kmplitert.core.LiteRTCompiler
import io.github.kmplitert.core.TFBuffer
import io.github.kmplitert.tool.LiteRTHandler
import io.github.kmplitert.tool.LiteRtImage
import io.github.kmplitert.tool.classification.Category
import io.github.kmplitert.tool.classification.ImageClassifier
import kmplitert.app.shared.generated.resources.Res

class MobileNetRunner : BaseLiteRTRunner<LiteRtImage, List<Category>>(
    modelResourcePath = "files/mobilenet_v1.tflite",
    accelerator = LiteRTAccelerator.CPU
) {
    private var classifier: ImageClassifier? = null
    private var labels: List<String> = emptyList()

    override suspend fun run(input: LiteRtImage): Result<List<Category>> {
        return try {
            ensureInitialized()
            
            if (labels.isEmpty()) {
                val labelsByte = Res.readBytes("files/mobilenet_v1_cn.txt")
                labels = labelsByte.decodeToString().lines()
            }

            if (classifier == null) {
                val handler = MobileNetHandler(labels)
                classifier = ImageClassifier(compiler!!, handler)
            }

            Result.success(classifier!!.classify(input))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    class MobileNetHandler(val labels: List<String>) : LiteRTHandler<LiteRtImage, List<Category>> {
        override suspend fun preprocess(
            input: LiteRtImage,
            compiler: LiteRTCompiler,
            inputBuffers: List<TFBuffer>
        ) {
            val resized = input.resize(224, 224).toRgb()
            val data = resized.toInt8Array()
            inputBuffers[0].writeInt8(data)
        }

        override suspend fun postprocess(
            outputBuffers: List<TFBuffer>,
            compiler: LiteRTCompiler
        ): List<Category> {
            val result = outputBuffers[0].readInt8()
            return result.mapIndexed { index, scoreByte ->
                val label = labels.getOrNull(index) ?: index.toString()
                val score = (scoreByte.toInt() and 0xFF).toFloat() / 255f
                Category(label, score, index)
            }
            .sortedByDescending { it.score }
            .take(5)
        }
    }
}
