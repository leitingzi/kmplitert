package io.github.leitingzi.kmplitert.core

import kotlinx.coroutines.suspendCancellableCoroutine
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.get
import org.w3c.xhr.ARRAYBUFFER
import org.w3c.xhr.XMLHttpRequest
import org.w3c.xhr.XMLHttpRequestResponseType
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

actual suspend fun loadResourceAsBytes(name: String): ByteArray = suspendCancellableCoroutine { continuation ->
    // Try multiple common Karma resource paths
    val paths = listOf(
        "/base/library/kmplitert-core/build/processedResources/js/test/$name",
        "/base/processedResources/js/test/$name",
        "/base/library/kmplitert-core/src/commonTest/resources/$name",
        "/base/$name",
        name
    )
    
    fun tryPath(index: Int) {
        if (index >= paths.size) {
            // Fallback to embedded model if specifically requested
            if (name == "CelsiusToFahrenheit.tflite") {
                continuation.resume(decodeBase64(CELSIUS_TO_FAHRENHEIT_MODEL_BASE64))
                return
            }
            continuation.resumeWithException(Exception("Resource not found after trying all paths: $name"))
            return
        }
        
        val url = paths[index]
        val currentXhr = XMLHttpRequest()
        currentXhr.open("GET", url, true)
        currentXhr.responseType = XMLHttpRequestResponseType.ARRAYBUFFER
        
        currentXhr.onload = {
            if (currentXhr.status == 200.toShort()) {
                val arrayBuffer = currentXhr.response as ArrayBuffer
                val int8Array = Int8Array(arrayBuffer)
                continuation.resume(ByteArray(int8Array.length) { i -> int8Array[i] })
            } else {
                tryPath(index + 1)
            }
        }
        
        currentXhr.onerror = {
            tryPath(index + 1)
        }
        
        currentXhr.send()
    }
    
    tryPath(0)
}

actual fun LiteRTAccelerator.isSupportedOnCurrentPlatform(): Boolean {
    return true
}

