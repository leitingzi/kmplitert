@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package org.example.kmplitert

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import android.provider.OpenableColumns
import java.io.File

@Composable
actual fun ModelFilePickerButton(
    enabled: Boolean,
    onModelSelected: (path: String, fileName: String) -> Unit,
    onError: (String) -> Unit,
    modifier: Modifier
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            try {
                val fileName = context.contentResolver.query(
                    uri,
                    arrayOf(OpenableColumns.DISPLAY_NAME),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                    } else {
                        null
                    }
                } ?: uri.lastPathSegment?.substringAfterLast('/') ?: "model.tflite"
                if (!fileName.endsWith(".tflite", ignoreCase = true)) {
                    onError("Choose a .tflite model file.")
                    return@rememberLauncherForActivityResult
                }
                val modelFile = File.createTempFile("kmplitert-model-", ".tflite", context.cacheDir)
                context.contentResolver.openInputStream(uri).use { input ->
                    requireNotNull(input) { "Could not read the selected model file." }.use { source ->
                        modelFile.outputStream().use(source::copyTo)
                    }
                }
                onModelSelected(modelFile.absolutePath, fileName)
            } catch (error: Exception) {
                onError("Could not read the selected model: ${error.message ?: "unknown error"}")
            }
        }
    }

    Button(
        onClick = { launcher.launch(arrayOf("*/*")) },
        enabled = enabled,
        modifier = modifier
    ) {
        Text("Choose .tflite file")
    }
}
