@file:OptIn(ExperimentalWasmJsInterop::class)
@file:Suppress("UNUSED_PARAMETER")

package io.github.kmplitert.file

import org.khronos.webgl.Uint8Array
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import org.khronos.webgl.set
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.toJsArray

internal actual fun writeModelFile(data: ByteArray, fileName: String): String {
    val uint8Array = Uint8Array(length = data.size)
    for (index in data.indices) {
        uint8Array[index] = data[index]
    }

    val blob = Blob(
        blobParts = arrayOf<JsAny?>(uint8Array).toJsArray(),
        options = BlobPropertyBag(type = "application/octet-stream")
    )
    return URL.createObjectURL(blob = blob)
}
