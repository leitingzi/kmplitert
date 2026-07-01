@file:OptIn(ExperimentalComposeUiApi::class)

package org.example.kmplitert

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport

fun main() = setContent {
    App()
}

fun setContent(content: @Composable () -> Unit) {
    ComposeViewport {
        WithFontResourcesLoaded {
            content()
        }
    }
}