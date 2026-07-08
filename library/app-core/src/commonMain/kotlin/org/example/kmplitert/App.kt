package org.example.kmplitert

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import io.github.leitingzi.kmplitert.core.LiteRTAccelerator
import io.github.leitingzi.kmplitert.core.LiteRTCompiler
import io.github.leitingzi.kmplitert.core.LiteRtImage
import kmplitert.library.app_core.generated.resources.Res
import kotlin.io.println

@Composable
fun App() {
    LaunchedEffect(Unit) {
        val modelPath = ComposeResourceUtils.getFilePath("mobilenet_v1.tflite")
        val compiler = LiteRTCompiler(filePath = modelPath, accelerator = LiteRTAccelerator.CPU)
        compiler.init()

        val inputs = compiler.getInputBuffers()
        val outputs = compiler.getOutputBuffers()
        println("inputs = ${inputs.size} | outputs = ${outputs.size}")

        val inputBuffer = inputs[0]

        val dogData = Res.readBytes("files/pic/elephant.bmp")
//        println("dogData = ${dogData.contentToString()}")
        val data = LiteRtImage.fromBytes(dogData)
            .resize(224, 224)
            .toInt8Array()

//        println("data = ${data.contentToString()}")

        inputBuffer.writeInt8(data)

        compiler.run(inputs = inputs, outputs = outputs)

        val result = outputs[0].readInt8()
//        println("result = ${result.contentToString()}")

        val rIndex = result.maxIndices()
        println("rIndex = $rIndex")

        compiler.close()

        val labelsByte = Res.readBytes("files/mobilenet_v1_cn.txt")
        val labels = labelsByte.decodeToString().lines()

        rIndex.forEach { index ->
            println("image recognition result = ${labels[index]}")
        }
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
