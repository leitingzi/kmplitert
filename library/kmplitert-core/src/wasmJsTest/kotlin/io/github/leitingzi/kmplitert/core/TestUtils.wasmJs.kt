@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.leitingzi.kmplitert.core

import io.github.leitingzi.kmplitert.core.model.isWebGPUSupported
import kotlinx.browser.window
import kotlinx.coroutines.await
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.get
import kotlin.js.ExperimentalWasmJsInterop

actual suspend fun loadResourceAsBytes(name: String): ByteArray {
    // Try some standard Karma paths
    val paths = listOf(
        "/base/processedResources/wasmJs/test/$name",
        "/base/processedResources/js/test/$name",
        "/base/library/kmplitert-core/build/processedResources/wasmJs/test/$name",
        "/base/library/kmplitert-core/build/processedResources/js/test/$name",
        "/base/library/kmplitert-core/src/commonTest/resources/$name",
        "/base/$name",
        name
    )
    
    for (url in paths) {
        try {
            val response = window.fetch(url).await<org.w3c.fetch.Response>()
            if (response.ok) {
                val buffer = response.arrayBuffer().await<ArrayBuffer>()
                val int8Array = Int8Array(buffer)
                return ByteArray(int8Array.length) { i -> int8Array[i] }
            }
        } catch (e: Throwable) {
            // Ignore
        }
    }
    
    // Fallback to embedded model if specifically requested
    if (name == "CelsiusToFahrenheit.tflite") {
        return decodeBase64(CELSIUS_TO_FAHRENHEIT_MODEL_BASE64)
    }
    
    throw Exception("Resource not found after trying all paths: $name")
}

actual fun LiteRTAccelerator.isSupportedOnCurrentPlatform(): Boolean {
    return when (this) {
        LiteRTAccelerator.CPU -> true
        LiteRTAccelerator.GPU -> isWebGPUSupported()
        LiteRTAccelerator.NPU -> false
    }
}

