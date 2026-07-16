@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.leitingzi.kmplitert.tool

import kotlinx.browser.window
import kotlinx.coroutines.await
import org.khronos.webgl.Int8Array
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.w3c.dom.url.URL
import org.w3c.fetch.Response
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import kotlin.js.Promise

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

    actual suspend fun readAsset(path: String): ByteArray {
        val response = window.asDynamic().fetch(path).unsafeCast<Promise<Response>>().await()
        if (!response.ok) {
            throw IllegalStateException("Failed to fetch asset at $path")
        }
        val arrayBuffer = response.arrayBuffer().await()
        val int8Array = Int8Array(arrayBuffer)
        val byteArray = ByteArray(int8Array.length)
        for (i in 0 until int8Array.length) {
            byteArray[i] = int8Array[i]
        }
        return byteArray
    }
}
