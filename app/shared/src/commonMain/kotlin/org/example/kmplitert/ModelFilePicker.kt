package org.example.kmplitert

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun ModelFilePickerButton(
    enabled: Boolean,
    onModelSelected: (path: String, fileName: String) -> Unit,
    onError: (String) -> Unit,
    modifier: Modifier = Modifier
)
