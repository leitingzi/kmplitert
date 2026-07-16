@file:OptIn(ExperimentalWasmJsInterop::class)
@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.leitingzi.kmplitert.tool

import kotlinx.browser.window
import kotlinx.coroutines.await
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.khronos.webgl.set
import org.w3c.dom.url.URL
import org.w3c.fetch.Response
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

    actual suspend fun readAsset(path: String): ByteArray {
        val response = window.fetch(path).await()
        if (!response.ok) {
            throw IllegalStateException("Failed to fetch asset at $path")
        }
        val arrayBuffer = response.arrayBuffer().await()
        val uint8Array = Uint8Array(arrayBuffer)
        val byteArray = ByteArray(uint8Array.length)
        for (i in 0 until uint8Array.length) {
            byteArray[i] = uint8Array[i]
        }
        return byteArray
    }
}
