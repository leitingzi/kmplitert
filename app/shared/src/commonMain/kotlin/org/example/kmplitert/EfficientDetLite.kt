package org.example.kmplitert

import io.github.kmplitert.core.LiteRTAccelerator
import io.github.kmplitert.core.LiteRTCompiler
import io.github.kmplitert.tool.LiteRTFileUtils
import io.github.kmplitert.tool.LiteRtImage
import io.github.kmplitert.tool.detection.ObjectDetector
import kmplitert.app.shared.generated.resources.Res

suspend fun testEfficientDet() {
    println("--- Starting EfficientDet-Lite0 Object Detection ---")
    
    val modelData = Res.readBytes("files/efficientdet_lite0.tflite")
    val filePath = LiteRTFileUtils.createFileFromByteArray(modelData, "efficientdet_lite0.tflite")
    
    // 1. Initialize Compiler
    val compiler = LiteRTCompiler(filePath = filePath, accelerator = LiteRTAccelerator.CPU)
    compiler.init()

    // 2. Setup Handler (Custom for EfficientDet)
    // Note: In a real app, you'd load COCO labels from a file
    val handler = EfficientDetHandler(scoreThreshold = 0.4f, iouThreshold = 0.5f)

    // 3. Create High-Level Detector
    val detector = ObjectDetector(compiler, handler)

    try {
        // 4. Load Image
        val picData = Res.readBytes("files/pic/elephant.bmp")
        val image = LiteRtImage.fromBytes(picData)

        // 5. Run Detection
        val results = detector.detect(image)

        // 6. Print Results
        if (results.isEmpty()) {
            println("No objects detected.")
        } else {
            results.forEachIndexed { index, detection ->
                val category = detection.categories.first()
                println("Object ${index + 1}: ${category.label} (score: ${category.score}) at ${detection.boundingBox}")
            }
        }
    } catch (e: Exception) {
        println("Detection failed: ${e.message}")
        e.printStackTrace()
    } finally {
        // 7. Cleanup
        detector.close()
    }
}
