package org.example.project.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp

@Composable
fun WritersRoomScreen(
    viewModel: WritersRoomViewModel,
    onBack: () -> Unit,
    onNavigateToRecordingStudio: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            },
        contentAlignment = Alignment.TopCenter
    ) {
        Card(
            modifier = Modifier.widthIn(max = 600.dp).padding(16.dp).offset(y = 64.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Writer's Room", style = MaterialTheme.typography.headlineMedium)

                val hasScriptDraft = uiState.generatedScript.isNotBlank() && !uiState.isSaved
                val isSaved = uiState.isSaved

                OutlinedTextField(
                    value = uiState.prompt,
                    onValueChange = { viewModel.updatePrompt(it) },
                    label = { Text("Prompt") },
                    modifier = Modifier.fillMaxWidth().height(120.dp), // Fixed height to prevent infinite expansion
                    maxLines = 4 // Limit visible lines
                )
                
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Text("Target Duration: ${uiState.durationSeconds}s", style = MaterialTheme.typography.labelMedium)
                    Slider(
                        value = uiState.durationSeconds.toFloat(),
                        onValueChange = { viewModel.updateDuration(it.toInt()) },
                        valueRange = 5f..60f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Generate Script Button
                val isGeneratePrimary = !hasScriptDraft && !isSaved
                Button(
                    onClick = { viewModel.generateScript() },
                    enabled = !uiState.isLoading && uiState.prompt.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(0.8f),
                    colors = if (isGeneratePrimary) ButtonDefaults.buttonColors() else ButtonDefaults.filledTonalButtonColors()
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Text("Generate Script")
                    }
                }

                if (uiState.errorMessage != null) {
                    Text(uiState.errorMessage!!, color = MaterialTheme.colorScheme.error)
                }

                if (uiState.generatedScript.isNotBlank() || uiState.isLoading) {
                    OutlinedTextField(
                        value = uiState.generatedScript,
                        onValueChange = { viewModel.updateScriptContent(it) },
                        label = { Text("Generated Script") },
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                    )

                    if (isSaved) {
                        // "Saved!" state
                        FilledTonalButton(
                            onClick = { },
                            enabled = false,
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            Text("Saved!")
                        }
                        
                        if (uiState.savedScriptId != null) {
                            Button(
                                onClick = { 
                                    viewModel.advanceToRecordingStage()
                                    onNavigateToRecordingStudio(uiState.savedScriptId!!) 
                                },
                                modifier = Modifier.fillMaxWidth(0.8f),
                                colors = ButtonDefaults.buttonColors() // Primary
                            ) {
                                Text("Go to Recording Studio")
                            }
                        }
                    } else {
                        // "Save Script" button
                        Button(
                            onClick = { viewModel.saveScript() },
                            enabled = uiState.generatedScript.isNotBlank(),
                            modifier = Modifier.fillMaxWidth(0.8f),
                            colors = ButtonDefaults.buttonColors() // Primary since it's a draft
                        ) {
                            Text("Save Script")
                        }
                    }
                }

                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth(0.8f),
                    colors = ButtonDefaults.filledTonalButtonColors()
                ) { 
                    Text("Go Home") 
                }
            }
        }
    }
}
