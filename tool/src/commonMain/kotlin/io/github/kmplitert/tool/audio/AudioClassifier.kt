package io.github.kmplitert.tool.audio

import io.github.kmplitert.core.LiteRTAccelerator
import io.github.kmplitert.core.LiteRTCompiler
import io.github.kmplitert.core.LiteRTElementType
import io.github.kmplitert.tool.classification.Category

/**
 * A high-level API for performing audio classification using LiteRT.
 *
 * @param compiler The [LiteRTCompiler] instance to use for inference.
 * @param options Configuration options for classification.
 */
class AudioClassifier(
    private val compiler: LiteRTCompiler,
    private val options: AudioClassifierOptions = AudioClassifierOptions()
) {

    /**
     * Classifies the given [LiteRtAudio].
     *
     * @param audio The input audio to classify.
     * @param inputName The name of the input tensor. Defaults to "0".
     * @param outputName The name of the output tensor. Defaults to "0".
     * @return A list of [Category] results, sorted by score descending.
     */
    suspend fun classify(
        audio: LiteRtAudio,
        inputName: String = "0",
        outputName: String = "0"
    ): List<Category> {
        val inputType = try {
            compiler.getInputTensorType(inputName)
        } catch (e: Exception) {
            if (inputName != "0") compiler.getInputTensorType("0") else throw e
        }

        val outputType = try {
            compiler.getOutputTensorType(outputName)
        } catch (e: Exception) {
            if (outputName != "0") compiler.getOutputTensorType("0") else throw e
        }

        val inputBuffers = compiler.getInputBuffers()
        val inputBuffer = inputBuffers[0]

        // Preprocessing
        val monoAudio = audio.toMono()
        
        // Handle resampling if required by model (we'd need a way to know model's expected sample rate)
        // For now assume user provides correct sample rate or model handles it.
        
        if (options.useMelSpectrogram) {
            val melSpec = SignalProcessing.melSpectrogram(
                monoAudio.toFloatArray(),
                monoAudio.sampleRate,
                options.fftSize,
                options.hopSize,
                options.numMels
            )
            // Flatten melSpec list of FloatArrays into a single FloatArray
            val flattened = FloatArray(melSpec.size * options.numMels)
            for (i in melSpec.indices) {
                melSpec[i].copyInto(flattened, i * options.numMels)
            }
            inputBuffer.writeFloat(flattened)
        } else {
            val floatData = monoAudio.toFloatArray()
            when (inputType.elementType) {
                LiteRTElementType.FLOAT -> inputBuffer.writeFloat(floatData)
                LiteRTElementType.INT8 -> {
                    val byteData = ByteArray(floatData.size) { (floatData[it] * 127).toInt().toByte() }
                    inputBuffer.writeInt8(byteData)
                }
                else -> throw UnsupportedOperationException("Unsupported input element type: ${inputType.elementType}")
            }
        }

        // Inference
        val outputBuffers = compiler.getOutputBuffers()
        compiler.run(inputBuffers, outputBuffers)

        // Postprocess
        val scores = when (outputType.elementType) {
            LiteRTElementType.FLOAT -> outputBuffers[0].readFloat()
            LiteRTElementType.INT8 -> {
                val bytes = outputBuffers[0].readInt8()
                FloatArray(bytes.size) { (bytes[it].toInt() and 0xFF).toFloat() / 255f }
            }
            else -> throw UnsupportedOperationException("Unsupported output element type: ${outputType.elementType}")
        }

        val results = scores.mapIndexed { index, score ->
            val label = options.labels?.getOrNull(index) ?: index.toString()
            Category(label, score, index)
        }
        .filter { it.score >= options.scoreThreshold }
        .sortedByDescending { it.score }

        return if (options.topK > 0) {
            results.take(options.topK)
        } else {
            results
        }
    }

    /**
     * Closes the underlying [LiteRTCompiler].
     */
    suspend fun close() {
        compiler.close()
    }

    companion object {
        /**
         * Creates an [AudioClassifier] from a model file.
         */
        suspend fun create(
            modelPath: String,
            accelerator: LiteRTAccelerator = LiteRTAccelerator.CPU,
            options: AudioClassifierOptions = AudioClassifierOptions()
        ): AudioClassifier {
            val compiler = LiteRTCompiler(modelPath, accelerator)
            compiler.init()
            return AudioClassifier(compiler, options)
        }
    }
}
