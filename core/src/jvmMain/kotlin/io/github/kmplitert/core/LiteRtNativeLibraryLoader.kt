package io.github.kmplitert.core

import com.sun.jna.NativeLibrary
import com.sun.jna.Platform
import java.io.File
import java.io.FileOutputStream
import java.nio.channels.FileChannel
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import java.security.MessageDigest
import java.util.logging.Level
import java.util.logging.Logger

internal object LiteRtNativeLibraryLoader {
    private const val NATIVE_PATH_PROPERTY = "kmplitert.native.path"
    private const val CACHE_SCHEMA = "v1"
    private val logger = Logger.getLogger("io.github.kmplitert.core.LiteRtLibrary")
    private val extractionLock = Any()

    data class LoadedLibraries(
        val mainLibrary: File,
        val pluginDirectory: File,
        val acceleratorPlugin: File?,
        val acceleratorPluginAvailable: Boolean = false
    )

    fun load(): LoadedLibraries {
        val extension = when {
            Platform.isWindows() -> "dll"
            Platform.isMac() -> "dylib"
            else -> "so"
        }
        val prefix = Platform.RESOURCE_PREFIX
        val mainName = "libLiteRt.$extension"
        val acceleratorPluginName = when {
            Platform.isMac() -> "libLiteRtMetalAccelerator.$extension"
            else -> "libLiteRtWebGpuAccelerator.$extension"
        }
        val artifactNames = buildList {
            add(mainName)
            add(acceleratorPluginName)
            if (Platform.isWindows()) {
                add("dxcompiler.dll")
                add("dxil.dll")
            }
            if (prefix == "linux-x86-64") {
                addAll(
                    listOf(
                        "libLiteRtCompilerPlugin_google_tensor.so",
                        "libLiteRtCompilerPlugin_IntelOpenvino.so",
                        "libLiteRtCompilerPlugin_Qualcomm.so"
                    )
                )
            }
        }

        val libraries = System.getProperty(NATIVE_PATH_PROPERTY)
            ?.takeIf(String::isNotBlank)
            ?.let { loadOverride(File(it), mainName, acceleratorPluginName, artifactNames) }
            ?: extractBundled(prefix, mainName, acceleratorPluginName, artifactNames)

        try {
            System.load(libraries.mainLibrary.absolutePath)
        } catch (failure: UnsatisfiedLinkError) {
            throw IllegalStateException(
                "Failed to load LiteRT native library '${libraries.mainLibrary}'. " +
                        "Set -D$NATIVE_PATH_PROPERTY to the main library file or its directory.",
                failure
            )
        }

        val plugin = libraries.acceleratorPlugin
        var acceleratorPluginAvailable = false
        if (plugin == null) {
            logger.warning("LiteRT accelerator plugin '$acceleratorPluginName' is unavailable; GPU requests will use CPU")
        } else {
            try {
                NativeLibrary.getInstance(plugin.absolutePath)
                acceleratorPluginAvailable = true
            } catch (failure: UnsatisfiedLinkError) {
                logger.log(
                    Level.WARNING,
                    "Failed to load optional LiteRT accelerator '${plugin.name}'; GPU requests will use CPU",
                    failure
                )
            }
        }

        return libraries.copy(acceleratorPluginAvailable = acceleratorPluginAvailable)
    }

    private fun loadOverride(
        override: File,
        mainName: String,
        acceleratorPluginName: String,
        artifactNames: List<String>
    ): LoadedLibraries {
        val main = if (override.isDirectory) File(override, mainName) else override
        require(main.isFile) {
            "LiteRT native library not found at '$main'. " +
                    "Set -D$NATIVE_PATH_PROPERTY to the main library file or its directory."
        }
        val directory = requireNotNull(main.absoluteFile.parentFile)
        artifactNames.forEach { name ->
            if (name != main.name && File(directory, name).isFile) {
                logger.fine("Using LiteRT native artifact from override directory: ${File(directory, name)}")
            }
        }
        val plugin = File(directory, acceleratorPluginName).takeIf(File::isFile)
        return LoadedLibraries(main.absoluteFile, directory, plugin)
    }

    private fun extractBundled(
        prefix: String,
        mainName: String,
        acceleratorPluginName: String,
        artifactNames: List<String>
    ): LoadedLibraries {
        val resources = artifactNames.mapNotNull { name ->
            findResource(prefix, name)?.let { resourcePath ->
                val metadata = inspectResource(name, resourcePath)
                metadata
            }
        }
        val mainResource = resources.firstOrNull { it.fileName == mainName }
            ?: throw IllegalStateException(
                "No bundled LiteRT native library is available for '$prefix'. " +
                        "Set -D$NATIVE_PATH_PROPERTY to the main library file or its directory."
            )
        val cacheKey = sha256(
            resources.joinToString("\n") { "${it.fileName}:${it.sha256}" }
                .toByteArray(Charsets.UTF_8)
        )
        val cacheRoot = File(
            File(System.getProperty("java.io.tmpdir"), "kmplitert"),
            "$CACHE_SCHEMA-$prefix-$cacheKey"
        )

        synchronized(extractionLock) {
            Files.createDirectories(cacheRoot.toPath())
            cacheRoot.deleteOnExit()
            val lockFile = File(cacheRoot, ".extract.lock")
            lockFile.deleteOnExit()
            FileChannel.open(
                lockFile.toPath(),
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE
            ).use { channel ->
                channel.lock().use {
                    resources.forEach { resource ->
                        extractResource(resource, File(cacheRoot, resource.fileName))
                    }
                }
            }
        }

        val main = File(cacheRoot, mainResource.fileName)
        val plugin = resources.firstOrNull { it.fileName == acceleratorPluginName }
            ?.let { File(cacheRoot, it.fileName) }
        return LoadedLibraries(main, cacheRoot, plugin)
    }

    private data class NativeResource(
        val fileName: String,
        val resourcePath: String,
        val length: Long,
        val sha256: String
    )

    private fun findResource(prefix: String, fileName: String): String? {
        val classLoader = LiteRtNativeLibraryLoader::class.java.classLoader
            ?: ClassLoader.getSystemClassLoader()
        val candidates = listOf("$prefix/$fileName", fileName)
        return candidates.firstOrNull { classLoader.getResource(it) != null }
    }

    private fun inspectResource(fileName: String, resourcePath: String): NativeResource {
        val classLoader = LiteRtNativeLibraryLoader::class.java.classLoader
            ?: ClassLoader.getSystemClassLoader()
        val digest = MessageDigest.getInstance("SHA-256")
        var length = 0L
        classLoader.getResourceAsStream(resourcePath).use { input ->
            requireNotNull(input) { "Native resource disappeared during loading: $resourcePath" }
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
                length += count
            }
        }
        return NativeResource(fileName, resourcePath, length, digest.digest().toHexString())
    }

    private fun extractResource(resource: NativeResource, target: File) {
        if (target.isFile && target.length() == resource.length) {
            target.deleteOnExit()
            return
        }

        val temporary = File.createTempFile(".${resource.fileName}.", ".tmp", target.parentFile)
        try {
            val classLoader = LiteRtNativeLibraryLoader::class.java.classLoader
                ?: ClassLoader.getSystemClassLoader()
            classLoader.getResourceAsStream(resource.resourcePath).use { input ->
                requireNotNull(input) { "Native resource disappeared during extraction: ${resource.resourcePath}" }
                FileOutputStream(temporary).use { output -> input.copyTo(output) }
            }
            try {
                Files.move(
                    temporary.toPath(),
                    target.toPath(),
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING
                )
            } catch (_: AtomicMoveNotSupportedException) {
                Files.move(temporary.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING)
            }
            target.deleteOnExit()
        } finally {
            if (temporary.exists()) temporary.delete()
        }
    }

    private fun sha256(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes).toHexString()

    private fun ByteArray.toHexString(): String =
        joinToString(separator = "") { byte -> "%02x".format(byte) }
}
