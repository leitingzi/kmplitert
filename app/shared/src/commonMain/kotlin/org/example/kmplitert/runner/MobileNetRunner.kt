package org.example.kmplitert.runner

import io.github.kmplitert.core.LiteRTAccelerator
import io.github.kmplitert.core.TFBuffer
import io.github.kmplitert.tool.LiteRTExt
import io.github.kmplitert.tool.LiteRTFileUtils
import io.github.kmplitert.tool.LiteRTHandler
import io.github.kmplitert.tool.image.LiteRtImage
import kmplitert.app.shared.generated.resources.Res

class MobileNetRunner: LiteRTHandler<LiteRtImage, List<LiteRTExt.Category>>() {
    
    private var labels: List<String> = emptyList()

    override suspend fun init() {
        // Load labels
        if (labels.isEmpty()) {
            val labelsByte = Res.readBytes("files/mobilenet_v1_cn.txt")
            labels = labelsByte.decodeToString().lines()
        }

        // Load model and setup compiler
        val modelResourcePath = "files/mobilenet_v1.tflite"
        val modelData = Res.readBytes(modelResourcePath)
        val modelName = modelResourcePath.substringAfterLast("/")
        val filePath = LiteRTFileUtils.createFileFromByteArray(modelData, modelName)

        setupCompiler(filePath, LiteRTAccelerator.CPU)
    }

    suspend fun classify(image: LiteRtImage): Result<List<LiteRTExt.Category>> {
        return try {
            Result.success(runTask(image))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun preprocess(input: LiteRtImage, inputBuffers: List<TFBuffer>) {
        val resized = input.resize(224, 224).toRgb()
        val data = resized.toInt8Array()
        inputBuffers[0].writeInt8(data)
    }

    override suspend fun postprocess(outputBuffers: List<TFBuffer>): List<LiteRTExt.Category> {
        val result = outputBuffers[0].readInt8()
        return result.mapIndexed { index, scoreByte ->
            val label = labels.getOrNull(index) ?: index.toString()
            val score = (scoreByte.toInt() and 0xFF).toFloat() / 255f
            LiteRTExt.Category(label, score, index)
        }
        .sortedByDescending { it.score }
        .take(5)
    }
}
