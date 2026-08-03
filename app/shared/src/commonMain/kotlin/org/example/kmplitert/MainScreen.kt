package org.example.kmplitert

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.kmplitert.tool.LiteRTHandler
import org.example.kmplitert.viewmodel.MainViewModel

@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel { MainViewModel() }
) {
    val scrollState = rememberScrollState()
    val status by viewModel.currentStatus.collectAsState()

    LaunchedEffect(viewModel.logs) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Scaffold(topBar = { TopBar() }) { padding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Left Panel: Model List
            ModelList(
                models = viewModel.models,
                selectedModel = viewModel.selectedModel,
                onModelSelect = viewModel::selectModel,
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            )

            VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Right Panel: Details and Controls
            ModelDetail(
                model = viewModel.selectedModel,
                status = status,
                isProcessing = viewModel.isProcessing,
                onInitialize = viewModel::initializeSelectedModel,
                onClose = viewModel::closeSelectedModel,
                onGetInfo = viewModel::getTensorInfo,
                onRun = viewModel::runInference,
                onClearLogs = viewModel::clearLogs,
                logs = viewModel.logs,
                scrollState = scrollState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
    }
}

@Composable
private fun TopBar() {
    CenterAlignedTopAppBar(
        title = { Text("KMP LiteRT Explorer", style = MaterialTheme.typography.titleLarge) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@Composable
private fun ModelList(
    models: List<ModelItem>,
    selectedModel: ModelItem?,
    onModelSelect: (ModelItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            "Models",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(models) { model ->
                val isSelected = model.id == selectedModel?.id
                ListItem(
                    headlineContent = { Text(model.name) },
                    supportingContent = { Text(model.description, maxLines = 1) },
                    modifier = Modifier
                        .clickable { onModelSelect(model) }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    colors = if (isSelected) {
                        ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    } else {
                        ListItemDefaults.colors()
                    }
                )
            }
        }
    }
}

@Composable
private fun ModelDetail(
    model: ModelItem?,
    status: LiteRTHandler.Status,
    isProcessing: Boolean,
    onInitialize: () -> Unit,
    onClose: () -> Unit,
    onGetInfo: (List<String>, List<String>) -> Unit,
    onRun: () -> Unit,
    onClearLogs: () -> Unit,
    logs: String,
    scrollState: ScrollState,
    modifier: Modifier = Modifier
) {
    if (model == null) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("Select a model to start", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    val inputNames = remember(model.id) { model.defaultInputNames.toMutableStateList() }
    val outputNames = remember(model.id) { model.defaultOutputNames.toMutableStateList() }

    val isInitialized = status !is LiteRTHandler.Status.Idle && status !is LiteRTHandler.Status.Initializing && status !is LiteRTHandler.Status.Error
    val statusText = when (status) {
        LiteRTHandler.Status.Idle -> "Status: Idle"
        LiteRTHandler.Status.Initializing -> "Status: Initializing..."
        LiteRTHandler.Status.Ready -> "Status: Ready"
        LiteRTHandler.Status.Running -> "Status: Running Inference..."
        LiteRTHandler.Status.Closing -> "Status: Closing..."
        is LiteRTHandler.Status.Error -> "Status: Error"
    }
    val statusColor = when (status) {
        LiteRTHandler.Status.Ready -> Color(0xFF4CAF50)
        LiteRTHandler.Status.Running -> MaterialTheme.colorScheme.primary
        is LiteRTHandler.Status.Error -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(model.name, style = MaterialTheme.typography.headlineSmall)
                Text(
                    statusText,
                    style = MaterialTheme.typography.labelMedium,
                    color = statusColor
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onInitialize,
                    enabled = !isProcessing && !isInitialized
                ) {
                    Icon(Icons.Default.Build, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Initialize")
                }

                if (isInitialized) {
                    OutlinedButton(
                        onClick = onClose,
                        enabled = !isProcessing,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Close, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Close")
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Tensor Controls
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Tensor Configuration", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    TensorConfigSection(
                        title = "Inputs",
                        names = inputNames,
                        modifier = Modifier.weight(1f)
                    )
                    TensorConfigSection(
                        title = "Outputs",
                        names = outputNames,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { onGetInfo(inputNames.toList(), outputNames.toList()) },
                    enabled = isInitialized && !isProcessing,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Default.Info, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Get Tensors Info")
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Execution
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onRun,
                enabled = isInitialized && !isProcessing,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp)
            ) {
                Icon(Icons.Default.PlayArrow, null)
                Spacer(Modifier.width(8.dp))
                Text("Run Inference")
            }
            
            OutlinedButton(
                onClick = onClearLogs,
                modifier = Modifier.height(56.dp)
            ) {
                Icon(Icons.Default.Clear, null)
            }
        }

        Spacer(Modifier.height(16.dp))

        if (isProcessing) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        Spacer(Modifier.height(24.dp))

        // Console
        Text("Console Output", style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(8.dp))
        Console(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            scrollState = scrollState,
            logs = logs
        )
    }
}

@Composable
private fun TensorConfigSection(
    title: String,
    names: MutableList<String>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            names.forEachIndexed { index, name ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { names[index] = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Tensor name") },
                        singleLine = true
                    )
                    IconButton(onClick = { names.removeAt(index) }) {
                        Icon(Icons.Default.Delete, "Remove", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
        TextButton(
            onClick = { names.add("") },
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Icon(Icons.Default.Add, null)
            Spacer(Modifier.width(4.dp))
            Text("Add $title")
        }
    }
}

@Composable
private fun Console(
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    logs: String = ""
) {
    Surface(
        modifier = modifier,
        color = Color(0xFF1E1E1E),
        shape = MaterialTheme.shapes.medium,
        border = CardDefaults.outlinedCardBorder()
    ) {
        Box(
            modifier = Modifier
                .padding(12.dp)
                .verticalScroll(scrollState)
        ) {
            Text(
                text = logs,
                color = Color(0xFF00FF00),
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                lineHeight = 20.sp
            )
        }
    }
}
