package com.leitz.kmplitert

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.leitz.kmplitert.compose.App

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "kmplitert",
    ) {
        App()
    }
}