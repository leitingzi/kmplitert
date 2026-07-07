@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package org.example.kmplitert

import kmplitert.library.app_core.generated.resources.Res

actual object ComposeResourceUtils {
    actual suspend fun getFilePath(resourcePath: String): String {
        return Res.getUri("files/$resourcePath")
    }
}