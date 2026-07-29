package org.example.kmplitert

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.example.kmplitert.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(viewModel: MainViewModel = viewModel { MainViewModel() }) {
    val scrollState = rememberScrollState()

    // Auto-scroll logs to bottom
    LaunchedEffect(viewModel.logs) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFFD0BCFF),
            secondary = Color(0xFFCCC2DC),
            tertiary = Color(0xFFEFB8C8)
        )
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("KMP LiteRT Explorer") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        scrolledContainerColor = Color.Unspecified,
                        navigationIconContentColor = Color.Unspecified,
                        titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        actionIconContentColor = Color.Unspecified
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Control Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Model Controls",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { viewModel.runMobileNet() },
                                enabled = !viewModel.isProcessing,
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text("MobileNet")
                            }

                            Button(
                                onClick = { viewModel.runEfficientDet() },
                                enabled = !viewModel.isProcessing,
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text("EfficientDet")
                            }

                            FilledIconButton(
                                onClick = { viewModel.clearLogs() },
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                )
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Clear")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Progress Indicator
                if (viewModel.isProcessing) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().height(4.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                } else {
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Log Console
                Text(
                    "Output Console",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    color = Color.Black,
                    shape = MaterialTheme.shapes.medium,
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Box(
                        modifier = Modifier
                            .padding(12.dp)
                            .verticalScroll(scrollState)
                    ) {
                        Text(
                            text = viewModel.logs,
                            color = Color(0xFF00FF00), // Terminal green
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}
