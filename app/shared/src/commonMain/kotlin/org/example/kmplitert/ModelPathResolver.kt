package org.example.kmplitert

import androidx.compose.runtime.Composable

@Composable
expect fun rememberModelPathResolver(): suspend (String) -> String
