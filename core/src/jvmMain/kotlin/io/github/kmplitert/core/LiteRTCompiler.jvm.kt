@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.kmplitert.core

import io.github.kmplitert.core.model.LiteRtCompiledModel
import io.github.kmplitert.core.model.LiteRtHwAcceleratorSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.net.JarURLConnection
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path

actual class LiteRTCompiler actual constructor(
    val filePath: String,
    val accelerator: LiteRTAccelerator
) {
    private data class TensorLocation(val signatureIndex: Long, val tensorIndex: Long)

    private val mutex = Mutex()
    private var compiledModel: LiteRtCompiledModel? = null
    private var temporaryModelFile: Path? = null
    private var inputLocations: Map<String, TensorLocation> = emptyMap()
    private var outputLocations: Map<String, TensorLocation> = emptyMap()
    private var closed = false

    actual suspend fun init() {
        withContext(Dispatchers.IO) {
            mutex.withLock {
                check(!closed) { "Compiler is closed" }
                if (compiledModel != null) return@withLock

                val normalizedPath = filePath.toLiteRtModelFilePath()
                val extractedModelFile = normalizedPath.extractJarResource()
                val modelPath = extractedModelFile?.toString() ?: normalizedPath
                val createdModel = try {
                    LiteRtCompiledModel.create(
                        filePath = modelPath,
                        accelerator = accelerator.toJvm()
                    )
                } catch (failure: Throwable) {
                    extractedModelFile?.deleteAfterFailure(failure)
                    throw failure
                }
                try {
                    val (inputs, outputs) = buildTensorLocations(createdModel)
                    compiledModel = createdModel
                    temporaryModelFile = extractedModelFile
                    inputLocations = inputs
                    outputLocations = outputs
                } catch (failure: Throwable) {
                    try {
                        createdModel.destroy()
                    } catch (cleanupFailure: Throwable) {
                        failure.addSuppressed(cleanupFailure)
                    }
                    extractedModelFile?.deleteAfterFailure(failure)
                    throw failure
                }
            }
        }
    }

    actual suspend fun getInputTensorType(inputName: String): LiteRTTensorType =
        withInitializedModel { model ->
            val location = inputLocation(inputName)
            model.modelOrThrow()
                .getSignature(location.signatureIndex)
                .getInputTensor(location.tensorIndex)
                .getRankedTensorType()
                .toPlatform()
        }

    actual suspend fun getOutputTensorType(outputName: String): LiteRTTensorType =
        withInitializedModel { model ->
            val location = outputLocation(outputName)
            model.modelOrThrow()
                .getSignature(location.signatureIndex)
                .getOutputTensor(location.tensorIndex)
                .getRankedTensorType()
                .toPlatform()
        }

    actual suspend fun getInputBufferRequirements(inputName: String): LiteRTBufferRequirements =
        withInitializedModel { model ->
            val location = inputLocation(inputName)
            val requirements = model.getInputBufferRequirements(
                signature_index = location.signatureIndex,
                input_index = location.tensorIndex
            ).toPlatform()
            if (requirements.strides.isEmpty()) {
                val tensorType = model.modelOrThrow()
                    .getSignature(location.signatureIndex)
                    .getInputTensor(location.tensorIndex)
                    .getRankedTensorType()
                    .toPlatform()
                requirements.copy(strides = tensorType.layout?.strides ?: emptyList())
            } else {
                requirements
            }
        }

    actual suspend fun getOutputBufferRequirements(outputName: String): LiteRTBufferRequirements =
        withInitializedModel { model ->
            val location = outputLocation(outputName)
            val requirements = model.getOutputBufferRequirements(
                signature_index = location.signatureIndex,
                output_index = location.tensorIndex
            ).toPlatform()
            if (requirements.strides.isEmpty()) {
                val tensorType = model.modelOrThrow()
                    .getSignature(location.signatureIndex)
                    .getOutputTensor(location.tensorIndex)
                    .getRankedTensorType()
                    .toPlatform()
                requirements.copy(strides = tensorType.layout?.strides ?: emptyList())
            } else {
                requirements
            }
        }

    actual suspend fun getInputBuffers(signatureIndex: Int): List<TFBuffer> =
        withInitializedModel { it.getInputBuffers(signatureIndex.toLong()) }

    actual suspend fun getOutputBuffers(signatureIndex: Int): List<TFBuffer> =
        withInitializedModel { it.getOutputBuffers(signatureIndex.toLong()) }

    actual suspend fun run(inputs: List<TFBuffer>, outputs: List<TFBuffer>, signatureIndex: Int) {
        withInitializedModel { model ->
            model.run(
                signature_index = signatureIndex.toLong(),
                input_buffers = inputs,
                output_buffers = outputs
            )
        }
    }

    actual suspend fun close() {
        withContext(NonCancellable + Dispatchers.IO) {
            mutex.withLock {
                if (closed) return@withLock
                closed = true
                val model = compiledModel
                compiledModel = null
                val modelFile = temporaryModelFile
                temporaryModelFile = null
                inputLocations = emptyMap()
                outputLocations = emptyMap()
                var failure: Throwable? = null
                try {
                    model?.destroy()
                } catch (destroyFailure: Throwable) {
                    failure = destroyFailure
                }
                try {
                    modelFile?.let { Files.deleteIfExists(it) }
                } catch (cleanupFailure: Throwable) {
                    if (failure == null) {
                        failure = cleanupFailure
                    } else {
                        failure.addSuppressed(cleanupFailure)
                    }
                }
                failure?.let { throw it }
            }
        }
    }

    private suspend fun <T> withInitializedModel(block: (LiteRtCompiledModel) -> T): T =
        withContext(Dispatchers.IO) {
            mutex.withLock {
                check(!closed) { "Compiler is closed" }
                val model = compiledModel ?: throw IllegalStateException("Compiler is not initialized")
                block(model)
            }
        }

    private fun buildTensorLocations(
        compiledModel: LiteRtCompiledModel
    ): Pair<Map<String, TensorLocation>, Map<String, TensorLocation>> {
        val model = compiledModel.modelOrThrow()
        val inputs = linkedMapOf<String, TensorLocation>()
        val outputs = linkedMapOf<String, TensorLocation>()
        var signatureIndex = 0L

        while (signatureIndex < model.getNumSignatures()) {
            val signature = model.getSignature(signatureIndex)
            var tensorIndex = 0L
            while (tensorIndex < signature.getNumInputs()) {
                val location = TensorLocation(signatureIndex, tensorIndex)
                inputs.putIfAbsent(signature.getInputName(tensorIndex), location)
                inputs.putIfAbsent(signature.getInputTensor(tensorIndex).getName(), location)
                tensorIndex++
            }
            tensorIndex = 0L
            while (tensorIndex < signature.getNumOutputs()) {
                val location = TensorLocation(signatureIndex, tensorIndex)
                outputs.putIfAbsent(signature.getOutputName(tensorIndex), location)
                outputs.putIfAbsent(signature.getOutputTensor(tensorIndex).getName(), location)
                tensorIndex++
            }
            signatureIndex++
        }
        return inputs to outputs
    }

    private fun inputLocation(name: String): TensorLocation =
        inputLocations[name] ?: throw IllegalArgumentException("Input tensor '$name' was not found")

    private fun outputLocation(name: String): TensorLocation =
        outputLocations[name] ?: throw IllegalArgumentException("Output tensor '$name' was not found")

    private fun LiteRtCompiledModel.modelOrThrow() =
        model ?: throw IllegalStateException("Model is not set")

    private fun LiteRTAccelerator.toJvm(): LiteRtHwAcceleratorSet =
        when (this) {
            LiteRTAccelerator.CPU -> LiteRtHwAcceleratorSet.CPU
            LiteRTAccelerator.GPU -> LiteRtHwAcceleratorSet.GPU
            LiteRTAccelerator.NPU -> LiteRtHwAcceleratorSet.NPU
        }

    private fun String.extractJarResource(): Path? {
        if (!startsWith("jar:")) return null

        val temporaryFile = Files.createTempFile("kmplitert-model-", ".tflite")
        try {
            val connection = URI(this).toURL().openConnection()
            if (connection is JarURLConnection) {
                connection.useCaches = false
            }
            connection.getInputStream().use { input ->
                Files.newOutputStream(temporaryFile).use { output ->
                    input.copyTo(output)
                }
            }
        } catch (failure: Throwable) {
            temporaryFile.deleteAfterFailure(failure)
            throw failure
        }
        return temporaryFile
    }

    private fun Path.deleteAfterFailure(failure: Throwable) {
        try {
            Files.deleteIfExists(this)
        } catch (cleanupFailure: Throwable) {
            failure.addSuppressed(cleanupFailure)
        }
    }
}
