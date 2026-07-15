@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.leitingzi.kmplitert.tool

import org.khronos.webgl.Uint8Array
import org.khronos.webgl.set
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag

actual object LiteRTFileUtils {
    actual fun createFileFromByteArray(data: ByteArray, fileName: String): String {
        val uint8Array = Uint8Array(data.size)
        for (i in data.indices) {
            uint8Array[i] = data[i]
        }
        val blob = Blob(
            arrayOf<JsAny?>(uint8Array).toJsArray(),
            BlobPropertyBag(type = "application/octet-stream")
        )
        return URL.createObjectURL(blob)
    }
}
