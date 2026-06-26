package com.leitz.kmplitert

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.leitz.kmplitert.compose.App
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine

fun runSuspend(block: suspend () -> Unit) {
    val continuation = object : Continuation<Unit> {
        override val context = EmptyCoroutineContext

        override fun resumeWith(result: Result<Unit>) {
            result.getOrThrow()
        }
    }
    block.startCoroutine(continuation)
}

fun main() = application {
    runSuspend {
        val modelFile = ResourceUtils.resourceToTempFile("CelsiusToFahrenheit.tflite")
        val compiler = LiteRTCompiler(filePath = modelFile.path, accelerator = LiteRTAccelerator.CPU)
        compiler.init()
        val inputBuffers = compiler.getInputBuffers()
        val outputBuffers = compiler.getOutputBuffers()
        inputBuffers[0].writeFloat(floatArrayOf(100f))
        compiler.run(inputBuffers, outputBuffers)
        val result = outputBuffers[0].readFloat()
        println("result: ${result.contentToString()}")
        compiler.close()

        modelFile.delete()
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "kmplitert",
    ) {
        App()
    }
}