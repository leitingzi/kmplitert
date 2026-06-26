package com.leitz.kmplitert

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.leitz.kmplitert.compose.App
import java.io.File
import java.io.InputStream
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine

fun runSuspend(block: suspend () -> Unit) {
    block.startCoroutine(object : Continuation<Unit> {
        override val context = EmptyCoroutineContext

        override fun resumeWith(result: Result<Unit>) {
            result.getOrThrow()
        }
    })
}

object ResourceUtils {
    fun getInputStream(filePath: String): InputStream {
        return this::class.java.getResourceAsStream(filePath)
            ?: throw Exception("Resource not found: $filePath")
    }

    fun resourceToTempFile(filePath: String): File {
        val input = getInputStream(filePath)
        val file = kotlin.io.path.createTempFile().toFile()

        input.use {
            file.outputStream().use(it::copyTo)
        }

        return file
    }
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