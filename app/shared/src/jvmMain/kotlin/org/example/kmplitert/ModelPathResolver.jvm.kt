@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package org.example.kmplitert

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberModelPathResolver(): suspend (String) -> String =
    remember { { uri -> uri } }
