@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.leitingzi.kmplitert.tool

import org.khronos.webgl.Uint8Array
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag

actual object LiteRTFileUtils {
    actual fun createFileFromByteArray(data: ByteArray, fileName: String): String {
        val uint8Array = Uint8Array(data.size)
        val dynamicArray = uint8Array.asDynamic()
        for (i in data.indices) {
            dynamicArray[i] = data[i]
        }
        val blob = Blob(arrayOf(uint8Array), BlobPropertyBag(type = "application/octet-stream"))
        return URL.createObjectURL(blob)
    }
}
