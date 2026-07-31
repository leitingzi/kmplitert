package org.example.kmplitert.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.kmplitert.core.LiteRTCompiler
import io.github.kmplitert.tool.image.LiteRtImage
import kmplitert.app.shared.generated.resources.Res
import kotlinx.coroutines.launch
import org.example.kmplitert.ModelItem
import org.example.kmplitert.runner.EfficientDetRunner
import org.example.kmplitert.runner.MobileNetRunner

class MainViewModel : ViewModel() {
    var logs by mutableStateOf("Ready.\n")
        private set

    var isProcessing by mutableStateOf(false)
        private set

    val models = listOf(
        ModelItem(
            id = "mobilenet",
            name = "MobileNet V1",
            description = "Image classification model.",
            runner = MobileNetRunner(),
            defaultInputNames = listOf("input"),
            defaultOutputNames = listOf("MobilenetV1/Predictions/Reshape_1")
        ),
        ModelItem(
            id = "efficientdet",
            name = "EfficientDet Lite0",
            description = "Object detection model.",
            runner = EfficientDetRunner(),
            defaultInputNames = listOf("images"),
            defaultOutputNames = listOf("output_0", "output_1")
        )
    )

    var selectedModel by mutableStateOf<ModelItem?>(models.first())
    
    // Track initialization state for each model
    var initializedModels = mutableStateMapOf<String, Boolean>()

    fun selectModel(model: ModelItem) {
        selectedModel = model
    }

    fun initializeSelectedModel() {
        val model = selectedModel ?: return
        viewModelScope.launch {
            isProcessing = true
            addLog("Initializing ${model.name}...")
            try {
                model.runner.init()
                initializedModels[model.id] = true
                addLog("${model.name} initialized successfully.")
            } catch (e: Exception) {
                addLog("Initialization failed: ${e.message}")
            } finally {
                isProcessing = false
            }
        }
    }

    fun closeSelectedModel() {
        val model = selectedModel ?: return
        if (initializedModels[model.id] != true) return

        viewModelScope.launch {
            isProcessing = true
            addLog("Closing ${model.name}...")
            try {
                model.runner.close()
                initializedModels.remove(model.id)
                addLog("${model.name} closed successfully.")
            } catch (e: Exception) {
                addLog("Closing failed: ${e.message}")
            } finally {
                isProcessing = false
            }
        }
    }

    fun getTensorInfo(inputNames: List<String>, outputNames: List<String>) {
        val model = selectedModel ?: return
        if (initializedModels[model.id] != true) {
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
            isProcessing = true
            addLog("--- Running ${model.name} ---")
            try {
                val picData = Res.readBytes("files/pic/elephant.bmp")
                val image = LiteRtImage.fromBytes(picData)
                
                when (val runner = model.runner) {
                    is MobileNetRunner -> {
                        runner.classify(image).onSuccess { results ->
                            results.forEachIndexed { index, category ->
                                addLog("Top ${index + 1}: ${category.label} (${category.score})")
                            }
                        }.onFailure { throw it }
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
                        }.onFailure { throw it }
                    }
                }
            } catch (e: Exception) {
                addLog("Execution Error: ${e.message}")
            } finally {
                isProcessing = false
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
