@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package org.example.kmplitert

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIViewController
import platform.UniformTypeIdentifiers.UTTypeData
import platform.darwin.NSObject

private class ModelDocumentPickerDelegate(
    private val onModelSelected: (String, String) -> Unit,
    private val onError: (String) -> Unit
) : NSObject(), UIDocumentPickerDelegateProtocol {
    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentsAtURLs: List<*>
    ) {
        val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL ?: return
        val path = url.path ?: return
        val fileName = url.lastPathComponent ?: "model.tflite"
        if (fileName.endsWith(".tflite", ignoreCase = true)) {
            onModelSelected(path, fileName)
        } else {
            onError("Choose a .tflite model file.")
        }
    }
}

@Composable
actual fun ModelFilePickerButton(
    enabled: Boolean,
    onModelSelected: (path: String, fileName: String) -> Unit,
    onError: (String) -> Unit,
    modifier: Modifier
) {
    val delegate = remember { mutableStateOf<ModelDocumentPickerDelegate?>(null) }
    Button(
        onClick = {
            val picker = UIDocumentPickerViewController(
                forOpeningContentTypes = listOf(UTTypeData),
                asCopy = true
            )
            delegate.value = ModelDocumentPickerDelegate(onModelSelected, onError)
            picker.delegate = delegate.value
            val presenter = UIApplication.sharedApplication.keyWindow?.rootViewController
                ?: return@Button
            presenter.presentViewController(picker, animated = true, completion = null)
        },
        enabled = enabled,
        modifier = modifier
    ) {
        Text("Choose .tflite file")
    }
}
