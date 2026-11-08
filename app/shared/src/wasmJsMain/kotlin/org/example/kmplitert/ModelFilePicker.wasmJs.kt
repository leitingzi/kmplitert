@file:OptIn(ExperimentalWasmJsInterop::class)
@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package org.example.kmplitert

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import kotlinx.browser.document
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import org.w3c.dom.url.URL

private var selectedModelUrl: String? = null

@Composable
actual fun ModelFilePickerButton(
    enabled: Boolean,
    onModelSelected: (path: String, fileName: String) -> Unit,
    onError: (String) -> Unit,
    modifier: Modifier
) {
    val input = remember {
        (document.createElement("input") as HTMLInputElement).apply {
            type = "file"
            accept = ".tflite,application/octet-stream"
            style.display = "none"
            addEventListener("change", { _: Event ->
                val file = files?.item(0)
                if (file != null) {
                    if (!file.name.endsWith(".tflite", ignoreCase = true)) {
                        onError("Choose a .tflite model file.")
                    } else {
                        selectedModelUrl?.let(URL::revokeObjectURL)
                        val url = URL.createObjectURL(blob = file)
                        selectedModelUrl = url
                        onModelSelected(url, file.name)
                    }
                }
            })
            document.body?.appendChild(this)
        }
    }

    Button(
        onClick = {
            input.value = ""
            input.click()
        },
        enabled = enabled,
        modifier = modifier
    ) {
        Text("Choose .tflite file")
    }
}
