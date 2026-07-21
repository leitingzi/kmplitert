package org.example.kmplitert

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import io.github.kmplitert.core.LiteRTAccelerator
import io.github.kmplitert.core.LiteRTCompiler
import io.github.kmplitert.tool.LiteRTFileUtils
import io.github.kmplitert.tool.LiteRtImage
import kmplitert.app.shared.generated.resources.Res

suspend fun testMobilenet() {
    val modelData = Res.readBytes("files/mobilenet_v1.tflite")
    val filePath = LiteRTFileUtils.createFileFromByteArray(modelData, "mobilenet_v1.tflite")
    println("filePath = $filePath")

    val compiler = LiteRTCompiler(filePath = filePath, accelerator = LiteRTAccelerator.CPU)
    compiler.init()

    // TODO Android Bug [litert_compiled_model_jni.cc:619] Unsupported element type in Kotlin: 3
//    val inputTensorType = compiler.getInputTensorType("input")
//    println("inputTensorType = $inputTensorType")
//
//    val outputTensorType = compiler.getOutputTensorType("MobilenetV1/Predictions/Reshape_1")
//    println("outputTensorType = $outputTensorType")

    val inputRequirements = compiler.getInputBufferRequirements("input")
    println("inputRequirements = $inputRequirements")

    val outputRequirements = compiler.getOutputBufferRequirements("MobilenetV1/Predictions/Reshape_1")
    println("outputRequirements = $outputRequirements")

    val inputs = compiler.getInputBuffers()
    val outputs = compiler.getOutputBuffers()
    println("inputs = ${inputs.size} | outputs = ${outputs.size}")

    val inputBuffer = inputs[0]

    val dogData = Res.readBytes("files/pic/elephant.bmp")
//    println("dogData = ${dogData.contentToString()}")
    val data = LiteRtImage.fromBytes(dogData)
        .resize(224, 224)
        .toRgb()
        .toInt8Array()

//    println("data = ${data.contentToString()}")

    inputBuffer.writeInt8(data)

    compiler.run(inputs = inputs, outputs = outputs)

    val result = outputs[0].readInt8()
//    println("result = ${result.contentToString()}")

    val rIndex = result.maxIndices()
    println("rIndex = $rIndex")

    compiler.close()

    val labelsByte = Res.readBytes("files/mobilenet_v1_cn.txt")
    val labels = labelsByte.decodeToString().lines()

    rIndex.forEach { index ->
        println("image recognition result = ${labels[index]}")
    }
}

suspend fun testCelsiusToFahrenheit() {
    val modelData = Res.readBytes("files/CelsiusToFahrenheit.tflite")
    val filePath = LiteRTFileUtils.createFileFromByteArray(modelData, "CelsiusToFahrenheit.tflite")
    println("filePath = $filePath")

    val compiler = LiteRTCompiler(filePath = filePath, accelerator = LiteRTAccelerator.CPU)
    compiler.init()

    val inputTensorType = compiler.getInputTensorType("input_c")
    println("inputTensorType = $inputTensorType")

    val outputTensorType = compiler.getOutputTensorType("Identity")
    println("outputTensorType = $outputTensorType")

    val inputRequirements = compiler.getInputBufferRequirements("input_c")
    println("inputRequirements = $inputRequirements")

    val outputRequirements = compiler.getOutputBufferRequirements("Identity")
    println("outputRequirements = $outputRequirements")

    val inputs = compiler.getInputBuffers()
    val outputs = compiler.getOutputBuffers()
    println("inputs = ${inputs.size} | outputs = ${outputs.size}")

    inputs[0].writeFloat(floatArrayOf(100f))
    compiler.run(inputs = inputs, outputs = outputs)
    val result = outputs[0].readFloat()
    println("result = ${result.contentToString()}")
    compiler.close()
}

@Composable
fun App() {
    LaunchedEffect(Unit) {
        testMobilenet()
    }
}

fun ByteArray.maxIndices(): List<Int> {
    if (isEmpty()) {
        return emptyList()
    }

    var max = this[0]
    val indices = mutableListOf(0)

    for (i in 1 until size) {
        when {
            this[i] > max -> {
                max = this[i]
                indices.clear()
                indices.add(i)
            }
            this[i] == max -> {
                indices.add(i)
            }
        }
    }

    return indices
}
