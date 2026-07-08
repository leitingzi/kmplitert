package org.example.kmplitert

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.io.PrintStream

fun main() {
    fixPrint()

    application {
        Window(
            onCloseRequest = { exitApplication() },
        ) {
            App()
        }
    }
}

fun fixPrint() {
    val printStream = PrintStream(System.out, true, "UTF-8")
    System.setOut(printStream)
}