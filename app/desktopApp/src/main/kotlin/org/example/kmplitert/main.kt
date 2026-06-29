package org.example.kmplitert

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.leitingzi.kmplitert.core.LiteRTAccelerator
import io.github.leitingzi.kmplitert.core.LiteRTCompiler
import org.example.kmplitert.app.core.App
import org.example.kmplitert.app.core.ResourceUtils
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
        val modelPath = ResourceUtils.getResourcePath("CelsiusToFahrenheit.tflite")

        val compiler = LiteRTCompiler(filePath = modelPath, accelerator = LiteRTAccelerator.CPU)
        compiler.init()

        val inputBuffers = compiler.getInputBuffers()
        val outputBuffers = compiler.getOutputBuffers()

        inputBuffers[0].writeFloat(floatArrayOf(100f))

        compiler.run(inputBuffers, outputBuffers)

        val result = outputBuffers[0].readFloat()
        println("result: ${result.contentToString()}")

        compiler.close()
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "kmplitert",
    ) {
        App()
    }
}