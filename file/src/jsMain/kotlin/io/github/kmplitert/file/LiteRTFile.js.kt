@file:Suppress("UNUSED_PARAMETER")

package io.github.kmplitert.file

import org.khronos.webgl.Uint8Array
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag

internal actual fun writeModelFile(data: ByteArray, fileName: String): String {
    val uint8Array = Uint8Array(length = data.size)
    val dynamicArray = uint8Array.asDynamic()
    for (index in data.indices) {
        dynamicArray[index] = data[index]
    }

    val blob = Blob(
        blobParts = arrayOf(uint8Array),
        options = BlobPropertyBag(type = "application/octet-stream")
    )
    return URL.createObjectURL(blob = blob)
}
