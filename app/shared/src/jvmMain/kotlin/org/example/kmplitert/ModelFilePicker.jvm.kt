@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package org.example.kmplitert

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

@Composable
actual fun ModelFilePickerButton(
    enabled: Boolean,
    onModelSelected: (path: String, fileName: String) -> Unit,
    onError: (String) -> Unit,
    modifier: Modifier
) {
    Button(
        onClick = {
            val dialog = FileDialog(null as Frame?, "Choose a .tflite model", FileDialog.LOAD)
            dialog.isVisible = true
            val directory = dialog.directory
            val fileName = dialog.file
            if (directory != null && fileName != null) {
                val file = File(directory, fileName)
                if (file.extension.equals("tflite", ignoreCase = true)) {
                    onModelSelected(file.absolutePath, file.name)
                } else {
                    onError("Choose a .tflite model file.")
                }
            }
        },
        enabled = enabled,
        modifier = modifier
    ) {
        Text("Choose .tflite file")
    }
}
