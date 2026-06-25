@file:OptIn(ExperimentalWasmJsInterop::class)

package com.leitz.kmplitert

import com.leitz.kmplitert.model.Accelerator
import com.leitz.kmplitert.model.CompiledModel
import com.leitz.kmplitert.model.Tensor
import kotlinx.coroutines.await
import org.khronos.webgl.Float32Array
import org.khronos.webgl.get
import org.khronos.webgl.toFloat32Array
import org.khronos.webgl.toInt32Array

actual class LiteRTCompiler {
    private lateinit var compiledModel: CompiledModel

    actual suspend fun init(filePath: String) {
        LiteRtInit.awaitInit()

        val load = loadAndCompile(
            model = filePath,
            compileOptions = Accelerator.create(Accelerator.CPU)
        )

        compiledModel = load.await()

        println("LiteRTCompiler $compiledModel")
    }

    actual suspend fun getInputBuffers(): List<TFBuffer> {
        val inputs = compiledModel.getInputDetails()
        val list = mutableListOf<TFBuffer>()
        for (i in 0 until inputs.length) {
            list.add(JsTFBuffer(inputs[i].shape))
        }
        println("getInputBuffers ${list.size}")
        return list
    }

    actual suspend fun getOutputBuffers(): List<TFBuffer> {
        val outputs = compiledModel.getOutputDetails()
        val list = mutableListOf<TFBuffer>()
        for (i in 0 until outputs.length) {
            list.add(JsTFBuffer(outputs[i].shape))
        }
        println("getOutputBuffers ${list.size}")
        return list
    }

    actual suspend fun run(inputs: List<TFBuffer>, outputs: List<TFBuffer>) {
        val data = floatArrayOf(100f).toFloat32Array()
        val tensor = Tensor(data, intArrayOf(1, 1).toInt32Array())
        val output = compiledModel.run(tensor)
        val outputData = output.await()
        val outputTensor = outputData[0]
        val outputArray = outputTensor.toTypedArray() as Float32Array
        val data1 = outputArray[0]
        println("output = $data1")
    }

    actual suspend fun close() {
        compiledModel.delete()
    }
}

