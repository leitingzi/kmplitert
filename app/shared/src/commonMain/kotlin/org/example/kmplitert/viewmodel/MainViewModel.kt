package org.example.kmplitert.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.kmplitert.tool.LiteRtImage
import kmplitert.app.shared.generated.resources.Res
import kotlinx.coroutines.launch
import org.example.kmplitert.runner.EfficientDetRunner
import org.example.kmplitert.runner.MobileNetRunner

class MainViewModel : ViewModel() {
    var logs by mutableStateOf("Ready.\n")
        private set

    var isProcessing by mutableStateOf(false)
        private set

    private val mobileNetRunner = MobileNetRunner()
    private val efficientDetRunner = EfficientDetRunner()

    fun runMobileNet() {
        viewModelScope.launch {
            isProcessing = true
            addLog("--- Running MobileNet ---")
            try {
                val dogData = Res.readBytes("files/pic/elephant.bmp")
                val image = LiteRtImage.fromBytes(dogData)
                mobileNetRunner.run(image).onSuccess { results ->
                    results.forEachIndexed { index, category ->
                        addLog("Top ${index + 1}: ${category.label} (${category.score})")
                    }
                }.onFailure {
                    addLog("Error: ${it.message}")
                }
            } catch (e: Exception) {
                addLog("Error: ${e.message}")
            } finally {
                isProcessing = false
            }
        }
    }

    fun runEfficientDet() {
        viewModelScope.launch {
            isProcessing = true
            addLog("--- Running EfficientDet ---")
            try {
                val picData = Res.readBytes("files/pic/elephant.bmp")
                val image = LiteRtImage.fromBytes(picData)
                efficientDetRunner.run(image).onSuccess { results ->
                    if (results.isEmpty()) {
                        addLog("No objects detected.")
                    } else {
                        results.forEachIndexed { index, detection ->
                            val category = detection.categories.first()
                            addLog("Obj ${index + 1}: ${category.label} (${category.score})")
                        }
                    }
                }.onFailure {
                    addLog("Error: ${it.message}")
                }
            } catch (e: Exception) {
                addLog("Error: ${e.message}")
            } finally {
                isProcessing = false
            }
        }
    }

    fun clearLogs() {
        logs = ""
    }

    private fun addLog(msg: String) {
        logs += "$msg\n"
    }

    override fun onCleared() {
        super.onCleared()
        
        viewModelScope.launch {
            mobileNetRunner.close()
            efficientDetRunner.close()
        }
    }
}
