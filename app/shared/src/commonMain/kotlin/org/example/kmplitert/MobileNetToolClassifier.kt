package org.example.kmplitert

import io.github.kmplitert.core.LiteRTAccelerator
import io.github.kmplitert.core.LiteRTCompiler
import io.github.kmplitert.core.TFBuffer
import io.github.kmplitert.tool.LiteRTFileUtils
import io.github.kmplitert.tool.LiteRTHandler
import io.github.kmplitert.tool.LiteRtImage
import io.github.kmplitert.tool.classification.Category
import io.github.kmplitert.tool.classification.ImageClassifier
import kmplitert.app.shared.generated.resources.Res

class MobileNetHandler(val labels: List<String>) : LiteRTHandler<LiteRtImage, List<Category>> {
    override suspend fun preprocess(
        input: LiteRtImage,
        compiler: LiteRTCompiler,
        inputBuffers: List<TFBuffer>
    ) {
        // 1. Resize the image to 224x224 as required by MobileNet V1
        val resized = input.resize(224, 224).toRgb()
        
        // 2. Convert to Int8 array (raw bytes)
        val data = resized.toInt8Array()
        
        // 3. Write to the first input buffer
        inputBuffers[0].writeInt8(data)
    }

    override suspend fun postprocess(
        outputBuffers: List<TFBuffer>,
        compiler: LiteRTCompiler
    ): List<Category> {
        // 1. Read Int8 output from the model
        val result = outputBuffers[0].readInt8()
        
        // 2. Convert to Category list with label mapping
        // For Int8 classification models, the score is typically (byte & 0xFF) / 255f
        return result.mapIndexed { index, scoreByte ->
            val label = labels.getOrNull(index) ?: index.toString()
            val score = (scoreByte.toInt() and 0xFF).toFloat() / 255f
            Category(label, score, index)
        }
        .sortedByDescending { it.score }
        .take(5) // Top 5 results
    }
}

suspend fun testMobilenetWithTool() {
    val modelData = Res.readBytes("files/mobilenet_v1.tflite")
    val filePath = LiteRTFileUtils.createFileFromByteArray(modelData, "mobilenet_v1.tflite")
    
    // 1. Initialize the compiler explicitly
    val compiler = LiteRTCompiler(filePath = filePath, accelerator = LiteRTAccelerator.CPU)
    compiler.init()

    // 2. Load labels
    val labelsByte = Res.readBytes("files/mobilenet_v1_cn.txt")
    val labels = labelsByte.decodeToString().lines()

    // 3. Create the classifier with the custom handler
    val classifier = ImageClassifier(
        compiler = compiler,
        handler = MobileNetHandler(labels)
    )

    try {
        val dogData = Res.readBytes("files/pic/elephant.bmp")
        val image = LiteRtImage.fromBytes(dogData)

        println("--- Starting Tool-based Classification (Manual LiteRTHandler) ---")
        val results = classifier.classify(image)
        
        results.forEachIndexed { index, category ->
            println("Top ${index + 1}: ${category.label} (score: ${category.score})")
        }
    } finally {
        classifier.close()
    }
}
