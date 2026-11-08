package org.example.kmplitert

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.kmplitert.core.LiteRTAccelerator
import io.github.kmplitert.core.LiteRTCompiler
import io.github.kmplitert.core.LiteRTElementType
import io.github.kmplitert.core.LiteRTTensorType
import io.github.kmplitert.core.TFBuffer
import kmplitert.app.shared.generated.resources.Res
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private data class DemoModel(
    val name: String,
    val description: String,
    val resourcePath: String? = null,
    val filePath: String? = null,
    val inputName: String = "",
    val outputName: String = "",
    val sampleInput: String = "100"
)

private data class ModelInfo(
    val input: LiteRTTensorType,
    val output: LiteRTTensorType
)

private val demoModels = listOf(
    DemoModel(
        name = "Celsius to Fahrenheit",
        description = "Convert one temperature value from Celsius to Fahrenheit.",
        resourcePath = "files/CelsiusToFahrenheit.tflite",
        inputName = "input_c",
        outputName = "Identity"
    ),
    DemoModel(
        name = "Batch temperature conversion",
        description = "Convert three Celsius values in a single inference.",
        resourcePath = "files/CelsiusToFahrenheitEx.tflite",
        inputName = "input_c",
        outputName = "Identity",
        sampleInput = "100, 0, -40"
    )
)

@Composable
fun MainScreen() {
    var selectedModel by remember { mutableStateOf(demoModels.first()) }
    var modelInfo by remember { mutableStateOf<ModelInfo?>(null) }
    var compiler by remember { mutableStateOf<LiteRTCompiler?>(null) }
    var inputTensorName by remember { mutableStateOf(selectedModel.inputName) }
    var outputTensorName by remember { mutableStateOf(selectedModel.outputName) }
    var inputValues by remember { mutableStateOf(selectedModel.sampleInput) }
    var result by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf("Loading model…") }
    var isRunning by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var isInspecting by remember { mutableStateOf(false) }
    var isModelMenuExpanded by remember { mutableStateOf(false) }
    var modelLoadAttempt by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(selectedModel, modelLoadAttempt) {
        modelInfo = null
        compiler = null
        result = null
        inputTensorName = selectedModel.inputName
        outputTensorName = selectedModel.outputName
        inputValues = selectedModel.sampleInput
        isLoading = true
        message = "Loading ${selectedModel.name}…"
        var loadedCompiler: LiteRTCompiler? = null

        try {
            val modelPath = selectedModel.filePath
                ?: Res.getUri(checkNotNull(selectedModel.resourcePath))
            val newCompiler = LiteRTCompiler(modelPath, LiteRTAccelerator.CPU)
            loadedCompiler = newCompiler
            newCompiler.init()
            compiler = newCompiler

            if (selectedModel.inputName.isNotBlank() && selectedModel.outputName.isNotBlank()) {
                modelInfo = ModelInfo(
                    input = newCompiler.getInputTensorType(selectedModel.inputName),
                    output = newCompiler.getOutputTensorType(selectedModel.outputName)
                )
                message = "Model ready"
            } else {
                message = "Model loaded. Enter its input and output tensor names to inspect it."
            }
            isLoading = false
            kotlinx.coroutines.awaitCancellation()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            message = "Could not load model: ${error.message ?: error::class.simpleName}"
        } finally {
            compiler = null
            try {
                withContext(NonCancellable + Dispatchers.Default) {
                    loadedCompiler?.close()
                }
            } catch (error: Exception) {
                message = "Could not close model: ${error.message ?: error::class.simpleName}"
            }
            isLoading = false
        }
    }

    fun inspectSelectedModel() {
        val activeCompiler = compiler ?: return
        val inputName = inputTensorName.trim()
        val outputName = outputTensorName.trim()
        if (inputName.isEmpty() || outputName.isEmpty()) {
            message = "Enter both tensor names before inspecting the model."
            return
        }

        isInspecting = true
        modelInfo = null
        result = null
        scope.launch {
            try {
                modelInfo = ModelInfo(
                    input = activeCompiler.getInputTensorType(inputName),
                    output = activeCompiler.getOutputTensorType(outputName)
                )
                message = "Model ready"
            } catch (error: Exception) {
                message = "Could not inspect tensors: ${error.message ?: error::class.simpleName}"
            } finally {
                isInspecting = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("LiteRT Model Playground") })
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 720.dp)
                    .fillMaxWidth()
            ) {
                Text("Run an on-device model", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Start with the built-in temperature model or choose your own .tflite file.",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(24.dp))

                Card(Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("1. Choose a model", style = MaterialTheme.typography.titleMedium)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { isModelMenuExpanded = true },
                                enabled = !isLoading && !isRunning && !isInspecting
                            ) {
                                Text(selectedModel.name)
                            }
                            DropdownMenu(
                                expanded = isModelMenuExpanded,
                                onDismissRequest = { isModelMenuExpanded = false }
                            ) {
                                demoModels.forEach { model ->
                                    DropdownMenuItem(
                                        text = { Text(model.name) },
                                        onClick = {
                                            isModelMenuExpanded = false
                                            selectedModel = model
                                        }
                                    )
                                }
                            }
                            ModelFilePickerButton(
                                enabled = !isLoading && !isRunning && !isInspecting,
                                onModelSelected = { path, fileName ->
                                    selectedModel = DemoModel(
                                        name = fileName,
                                        description = "User-selected LiteRT model.",
                                        filePath = path
                                    )
                                },
                                onError = { message = it }
                            )
                        }
                        Text(selectedModel.description, style = MaterialTheme.typography.bodyMedium)

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isLoading || isInspecting) {
                                CircularProgressIndicator()
                            }
                            Text(
                                message,
                                color = if (message.startsWith("Could not")) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }

                        if (!isLoading && compiler == null) {
                            OutlinedButton(onClick = { modelLoadAttempt++ }) {
                                Text("Retry loading model")
                            }
                        }

                        if (!isLoading && compiler != null) {
                            Text("2. Inspect model tensors", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Enter the tensor names used by the selected model.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            OutlinedTextField(
                                value = inputTensorName,
                                onValueChange = {
                                    inputTensorName = it
                                    modelInfo = null
                                    result = null
                                },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Input tensor name") },
                                singleLine = true,
                                enabled = !isInspecting && !isRunning
                            )
                            OutlinedTextField(
                                value = outputTensorName,
                                onValueChange = {
                                    outputTensorName = it
                                    modelInfo = null
                                    result = null
                                },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Output tensor name") },
                                singleLine = true,
                                enabled = !isInspecting && !isRunning
                            )
                            Button(
                                onClick = ::inspectSelectedModel,
                                enabled = !isInspecting && !isRunning,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Read tensor information")
                            }
                        }

                        modelInfo?.let { info ->
                            Text(
                                "Input: $inputTensorName · ${info.input.elementType} · " +
                                    info.input.layout?.dimensions,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "Output: $outputTensorName · ${info.output.elementType} · " +
                                    info.output.layout?.dimensions,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text("3. Enter input data", style = MaterialTheme.typography.titleMedium)
                            OutlinedTextField(
                                value = inputValues,
                                onValueChange = {
                                    inputValues = it
                                    result = null
                                },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Input values") },
                                supportingText = {
                                    Text("Enter numeric values separated by commas.")
                                },
                                singleLine = true,
                                enabled = !isRunning
                            )
                            Button(
                                onClick = {
                                    val parsedValues = inputValues
                                        .split(',')
                                        .map { it.trim().toFloatOrNull() }
                                    if (parsedValues.any { it == null }) {
                                        result = "Enter valid numeric values separated by commas."
                                        return@Button
                                    }
                                    val values = parsedValues.filterNotNull()
                                    val expectedCount = info.input.layout?.dimensions
                                        ?.takeIf { dimensions -> dimensions.all { it > 0 } }
                                        ?.fold(1) { count, dimension -> count * dimension }
                                    if (expectedCount != null && values.size != expectedCount) {
                                        result = "Enter $expectedCount numeric value(s)."
                                        return@Button
                                    }
                                    if (values.isEmpty()) {
                                        result = "Enter at least one numeric value."
                                        return@Button
                                    }
                                    if (info.input.elementType !in supportedElementTypes) {
                                        result = "Input type ${info.input.elementType} is not supported by this demo."
                                        return@Button
                                    }

                                    val activeCompiler = compiler ?: return@Button
                                    isRunning = true
                                    result = null
                                    scope.launch {
                                        try {
                                            val inputs = activeCompiler.getInputBuffers()
                                            val outputs = activeCompiler.getOutputBuffers()
                                            require(inputs.size == 1 && outputs.size == 1) {
                                                "This demo runs models with one input tensor and one output tensor."
                                            }
                                            writeInput(inputs.single(), info.input.elementType, values)
                                            activeCompiler.run(inputs, outputs)
                                            result = readOutput(outputs.single(), info.output.elementType)
                                        } catch (error: Exception) {
                                            result = "Inference failed: ${error.message ?: error::class.simpleName}"
                                        } finally {
                                            isRunning = false
                                        }
                                    }
                                },
                                enabled = compiler != null && !isRunning && !isInspecting,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(if (isRunning) "Running…" else "Run inference")
                            }
                            result?.let { output ->
                                Text("Result: $output", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}

private val supportedElementTypes = setOf(
    LiteRTElementType.FLOAT,
    LiteRTElementType.INT,
    LiteRTElementType.INT8,
    LiteRTElementType.UINT8,
    LiteRTElementType.INT64,
    LiteRTElementType.BOOLEAN
)

private fun writeInput(buffer: TFBuffer, elementType: LiteRTElementType, values: List<Float>) {
    when (elementType) {
        LiteRTElementType.FLOAT -> buffer.writeFloat(values.toFloatArray())
        LiteRTElementType.INT -> {
            require(values.all { it % 1f == 0f }) { "Integer tensors require whole-number inputs." }
            buffer.writeInt(values.map(Float::toInt).toIntArray())
        }
        LiteRTElementType.INT8 -> {
            require(values.all {
                it % 1f == 0f && it >= Byte.MIN_VALUE.toFloat() && it <= Byte.MAX_VALUE.toFloat()
            }) {
                "INT8 tensors require whole numbers between -128 and 127."
            }
            buffer.writeInt8(values.map { it.toInt().toByte() }.toByteArray())
        }
        LiteRTElementType.UINT8 -> {
            require(values.all { it % 1f == 0f && it >= 0f && it <= 255f }) {
                "UINT8 tensors require whole numbers between 0 and 255."
            }
            buffer.writeInt8(values.map { it.toInt().toByte() }.toByteArray())
        }
        LiteRTElementType.INT64 -> {
            require(values.all { it % 1f == 0f }) { "INT64 tensors require whole-number inputs." }
            buffer.writeLong(values.map(Float::toLong).toLongArray())
        }
        LiteRTElementType.BOOLEAN -> {
            require(values.all { it == 0f || it == 1f }) { "Boolean tensors accept only 0 or 1." }
            buffer.writeBoolean(values.map { it == 1f }.toBooleanArray())
        }
        else -> error("Input type $elementType is not supported by this demo.")
    }
}

private suspend fun readOutput(buffer: TFBuffer, elementType: LiteRTElementType): String =
    when (elementType) {
        LiteRTElementType.FLOAT -> buffer.readFloat().joinToString()
        LiteRTElementType.INT -> buffer.readInt().joinToString()
        LiteRTElementType.INT8 -> buffer.readInt8().joinToString()
        LiteRTElementType.UINT8 -> buffer.readInt8().joinToString { (it.toInt() and 0xff).toString() }
        LiteRTElementType.INT64 -> buffer.readLong().joinToString()
        LiteRTElementType.BOOLEAN -> buffer.readBoolean().joinToString()
        else -> "Output type $elementType is not supported by this demo."
    }
