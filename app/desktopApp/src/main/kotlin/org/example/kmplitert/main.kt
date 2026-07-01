package org.example.kmplitert

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
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
    Window(
        onCloseRequest = ::exitApplication,
        title = "kmplitert",
    ) {
        App()
    }
}