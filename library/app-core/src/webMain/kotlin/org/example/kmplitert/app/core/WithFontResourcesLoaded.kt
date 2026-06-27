package org.example.kmplitert.app.core

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.font.FontFamily
import kmplitert.library.app_core.generated.resources.NotoSansSC
import kmplitert.library.app_core.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.preloadFont

@OptIn(ExperimentalResourceApi::class)
@Composable
fun WithFontResourcesLoaded(content: @Composable () -> Unit) {
    val font by preloadFont(Res.font.NotoSansSC)

    var fontFallbackInitialized by remember { mutableStateOf(false) }
    val fontFamilyResolver = LocalFontFamilyResolver.current

    LaunchedEffect(fontFamilyResolver, font) {
        font?.let { font ->
            fontFamilyResolver.preload(FontFamily(font))
            fontFallbackInitialized = true
        }
    }

    if (fontFallbackInitialized) {
        content()
    }
}