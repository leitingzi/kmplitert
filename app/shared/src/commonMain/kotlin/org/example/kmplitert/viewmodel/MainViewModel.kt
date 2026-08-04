package org.example.kmplitert.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.kmplitert.core.LiteRTCompiler
import io.github.kmplitert.tool.*
import io.github.kmplitert.tool.image.LiteRtImage
import io.github.kmplitert.tool.interceptor.LiteRTInterceptionException
import kmplitert.app.shared.generated.resources.Res
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.kmplitert.ModelItem
import org.example.kmplitert.runner.EfficientDetRunner
import org.example.kmplitert.runner.MobileNetRunner

class MainViewModel : ViewModel() {
    var logs by mutableStateOf("Ready.\n")
        private set

    private val _currentStatus = MutableStateFlow<LiteRTHandler.Status>(LiteRTHandler.Status.Idle)
    val currentStatus: StateFlow<LiteRTHandler.Status> = _currentStatus.asStateFlow()

    var isProcessing by mutableStateOf(false)
        private set

    val models = listOf(
        ModelItem(
            id = "mobilenet",
            name = "MobileNet V1",
            description = "Image classification model.",
            runner = MobileNetRunner().apply {
                addImageShapeValidator(
                    expectedShape = intArrayOf(224, 228, 3),
                    onValidated = { shape -> addLog("[MobileNet] Shape validated: ${shape.contentToString()}") },
                    onInvalidated = { expected, actual -> addLog("[MobileNet] Shape check failed! Expected: ${expected.contentToString()}, Actual: ${actual.contentToString()}") }
                )
                addCache(onCacheHit = { _, _ -> addLog("[MobileNet] Cache Hit!") })
                addLogging(tag = "MobileNet", logger = ::addLog)
            },
            defaultInputNames = listOf("input"),
            defaultOutputNames = listOf("MobilenetV1/Predictions/Reshape_1")
        ),
        ModelItem(
            id = "efficientdet",
            name = "EfficientDet Lite0",
            description = "Object detection model.",
            runner = EfficientDetRunner().apply {
                addImageShapeValidator(
                    expectedShape = intArrayOf(320, 320, 3),
                    onValidated = { shape -> addLog("[EfficientDet] Shape validated: ${shape.contentToString()}") },
                    onInvalidated = { expected, actual -> addLog("[EfficientDet] Shape check failed! Expected: ${expected.contentToString()}, Actual: ${actual.contentToString()}") }
                )
                addCache(onCacheHit = { _, _ -> addLog("[EfficientDet] Cache Hit!") })
                addLogging(tag = "EfficientDet", logger = ::addLog)
            },
            defaultInputNames = listOf("images"),
            defaultOutputNames = listOf("output_0", "output_1")
        )
    )

    var selectedModel by mutableStateOf<ModelItem?>(models.first())

    private var statusJob: Job? = null
    private var testImage: LiteRtImage? = null // Cache the test image for result caching demo

    init {
        // Initial setup for the first model
        observeModelStatus(models.first())
    }

    fun selectModel(model: ModelItem) {
        selectedModel = model
        observeModelStatus(model)
    }

    private fun observeModelStatus(model: ModelItem) {
        statusJob?.cancel()
        statusJob = viewModelScope.launch {
            model.runner.status.collect { status ->
                _currentStatus.value = status
                when (status) {
                    is LiteRTHandler.Status.Error -> {
                        addLog("Error: ${status.throwable.message}")
                        isProcessing = false
                    }
                    is LiteRTHandler.Status.Ready -> {
                        isProcessing = false
                    }
                    is LiteRTHandler.Status.Running -> isProcessing = true
                    is LiteRTHandler.Status.Initializing -> isProcessing = true
                    is LiteRTHandler.Status.Closing -> isProcessing = true
                    is LiteRTHandler.Status.Idle -> isProcessing = false
                }
            }
        }
    }

    fun initializeSelectedModel() {
        val model = selectedModel ?: return
        viewModelScope.launch {
            addLog("Initializing ${model.name}...")
            try {
                model.runner.init()
                addLog("${model.name} initialized successfully.")
            } catch (_: Exception) {
                // Error status is updated via flow
            }
        }
    }

    fun closeSelectedModel() {
        val model = selectedModel ?: return
        viewModelScope.launch {
            addLog("Closing ${model.name}...")
            try {
                model.runner.close()
                addLog("${model.name} closed successfully.")
            } catch (_: Exception) {
                // Error status is updated via flow
            }
        }
    }

    fun getTensorInfo(inputNames: List<String>, outputNames: List<String>) {
        val model = selectedModel ?: return
        if (currentStatus.value !is LiteRTHandler.Status.Ready) {
            addLog("Error: Model must be initialized first.")
            return
        }

        viewModelScope.launch {
            try {
                val compiler = getCompiler(model)
                if (compiler == null) {
                    addLog("Error: Could not access compiler.")
                    return@launch
                }

                addLog("--- Tensor Info for ${model.name} ---")
                
                inputNames.forEach { name ->
                    if (name.isBlank()) return@forEach
                    try {
                        val inputType = compiler.getInputTensorType(name.trim())
                        addLog("Input [$name]: Type=${inputType.elementType}, Shape=${inputType.layout?.dimensions}")
                    } catch (e: Exception) {
                        addLog("Input [$name] error: ${e.message}")
                    }
                }

                outputNames.forEach { name ->
                    if (name.isBlank()) return@forEach
                    try {
                        val outputType = compiler.getOutputTensorType(name.trim())
                        addLog("Output [$name]: Type=${outputType.elementType}, Shape=${outputType.layout?.dimensions}")
                    } catch (e: Exception) {
                        addLog("Output [$name] error: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                addLog("Error fetching tensor info: ${e.message}")
            }
        }
    }

    fun runInference() {
        val model = selectedModel ?: return
        viewModelScope.launch {
            addLog("--- Running ${model.name} ---")
            try {
                if (testImage == null) {
                    val picData = Res.readBytes("files/pic/elephant.bmp")
                    testImage = LiteRtImage.fromBytes(picData)
                }
                val image = testImage!!
                
                when (val runner = model.runner) {
                    is MobileNetRunner -> {
                        runner.classify(image).onSuccess { results ->
                            results.forEachIndexed { index, category ->
                                addLog("Top ${index + 1}: ${category.label} (${category.score})")
                            }
                        }.onFailure { 
                            if (it !is LiteRTInterceptionException) {
                                addLog("Error: ${it.message}")
                            }
                        }
                    }
                    is EfficientDetRunner -> {
                        runner.detect(image).onSuccess { results ->
                            if (results.isEmpty()) {
                                addLog("No objects detected.")
                            } else {
                                results.forEachIndexed { index, detection ->
                                    val category = detection.categories.first()
                                    addLog("Obj ${index + 1}: ${category.label} (${category.score})")
                                }
                            }
                        }.onFailure { 
                            if (it !is LiteRTInterceptionException) {
                                addLog("Error: ${it.message}")
                            }
                        }
                    }
                }
            } catch (_: Exception) {
                // Error status is updated via flow
            }
        }
    }

    private fun getCompiler(model: ModelItem): LiteRTCompiler? {
        return when (val runner = model.runner) {
            is MobileNetRunner -> runner.compilerInstance
            is EfficientDetRunner -> runner.compilerInstance
            else -> null
        }
    }

    fun clearLogs() {
        logs = ""
    }

    fun addLog(msg: String) {
        logs += "$msg\n"
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            models.forEach { it.runner.close() }
        }
    }
}
