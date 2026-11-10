package io.github.kmplitert.file

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.IOException

private val contextLock = Any()
private var applicationContext: Context? = null
private val resolvedComposeUris = mutableMapOf<String, String>()

@Suppress("unused", "UnusedReceiverParameter")
fun LiteRTFile.init(context: Context) {
    synchronized(contextLock) {
        applicationContext = context.applicationContext
        resolvedComposeUris.clear()
    }
}

internal actual fun resolveCommonResource(filePath: String): String {
    val classLoader = LiteRTFile::class.java.classLoader ?: ClassLoader.getSystemClassLoader()
    val input = checkNotNull(classLoader.getResourceAsStream(filePath)) {
        "Common resource '$filePath' was not found on the Android classpath."
    }
    val extension = filePath.substringAfterLast('.', "").takeIf(String::isNotEmpty)
        ?.let { ".$it" } ?: ".tmp"
    val temporaryFile = File.createTempFile("kmplitert-", extension)
    var copied = false
    try {
        input.use { source ->
            temporaryFile.outputStream().use(source::copyTo)
        }
        copied = true
        return temporaryFile.absolutePath
    } finally {
        if (!copied) {
            temporaryFile.delete()
        }
    }
}

internal actual fun resolveComposeUri(uri: String): String = synchronized(contextLock) {
    resolvedComposeUris[uri]?.takeIf { File(it).isFile } ?: run {
        val context = checkNotNull(applicationContext) {
            "Call LiteRTFile.init(context) before resolving Compose resource URIs on Android."
        }
        val parsedUri = Uri.parse(uri)
        val assetPath = parsedUri.path
            ?.takeIf { parsedUri.scheme == "file" && it.startsWith("/android_asset/") }
            ?.removePrefix("/android_asset/")

        if (assetPath != null) {
            require(assetPath.isNotBlank() && assetPath.split('/').none { it == "." || it == ".." }) {
                "Invalid Compose Android asset URI: $uri"
            }
            val extension = assetPath.substringAfterLast('.', "").takeIf(String::isNotEmpty)
                ?.let { ".$it" } ?: ".tmp"
            val modelFile = File.createTempFile("kmplitert-model-", extension, context.cacheDir)
            var copied = false
            try {
                context.assets.open(assetPath).use { input ->
                    modelFile.outputStream().use(input::copyTo)
                }
                copied = true
                modelFile.absolutePath
            } finally {
                if (!copied) {
                    modelFile.delete()
                }
            }
        } else {
            require(parsedUri.scheme == "file") {
                "Compose resource URI must identify a local file or Android asset: $uri"
            }
            val modelFile = File(
                decodeUriPath(checkNotNull(parsedUri.path) { "Compose resource URI has no path: $uri" })
            )
            check(modelFile.isFile) { "Compose resource file does not exist: ${modelFile.path}" }
            modelFile.absolutePath
        }
    }.also { resolvedComposeUris[uri] = it }
}

internal actual fun writeModelFile(data: ByteArray, fileName: String): String {
    val directory = synchronized(contextLock) { applicationContext?.cacheDir }
        ?: File(System.getProperty("java.io.tmpdir") ?: ".")
    if (!directory.exists() && !directory.mkdirs() && !directory.isDirectory) {
        throw IllegalStateException("Could not create model directory: ${directory.absolutePath}")
    }

    val modelFile = File(directory, fileName)
    try {
        modelFile.writeBytes(data)
    } catch (error: IOException) {
        throw IllegalStateException("Could not write model file: ${modelFile.absolutePath}", error)
    }
    return modelFile.absolutePath
}
