package org.example.project.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EditingStudioScreen(
    viewModel: EditingStudioViewModel,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onNavigateToPublishingStudio: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val seekRequest by viewModel.seekRequest.collectAsState()

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Card(
            modifier = Modifier.widthIn(max = 600.dp).fillMaxHeight(0.9f).padding(16.dp).offset(y = 64.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top section
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Editing Studio", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "Tap on a clip to mark it for removal. E.g., remove the white space or mistakes.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    // Video Player
                    if (uiState.videoPath != null) {
                        Box(modifier = Modifier.fillMaxWidth().height(300.dp).background(Color.Black)) {
                            VideoPlayer(
                                modifier = Modifier.fillMaxSize(),
                                url = uiState.videoPath!!,
                                seekRequest = seekRequest,
                                onSeekHandled = { viewModel.clearSeekRequest() },
                                onTimeUpdate = { viewModel.onTimeUpdate(it) }
                            )
                        }
                    }

                    // Timeline simulation
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(uiState.segments) { segment ->
                            val backgroundColor = if (segment.markedForRemoval) {
                                MaterialTheme.colorScheme.errorContainer
                            } else {
                                MaterialTheme.colorScheme.primaryContainer
                            }

                            val textColor = if (segment.markedForRemoval) {
                                MaterialTheme.colorScheme.onErrorContainer
                            } else {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(60.dp)
                                    .clip(MaterialTheme.shapes.medium)
                                    .background(backgroundColor)
                                    .clickable { viewModel.toggleRemovalMarker(segment.second) },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${segment.second}s",
                                        color = textColor,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                    )
                                    if (segment.markedForRemoval) {
                                        Spacer(Modifier.height(4.dp))
                                        Text("Skip", color = Color.Red, fontSize = 10.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold)
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { viewModel.saveModifications() },
                            enabled = uiState.segments.any { it.markedForRemoval } && !uiState.isSaved && !uiState.isSaving,
                            modifier = Modifier.weight(1f).padding(end = 8.dp),
                            colors = if (uiState.isSaved) ButtonDefaults.filledTonalButtonColors() else ButtonDefaults.buttonColors()
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                            } else {
                                Text(if (uiState.isSaved) "Saved!" else "Save Edits")
                            }
                        }

                        Button(
                            onClick = { viewModel.restoreOriginal() },
                            enabled = uiState.segments.any { it.markedForRemoval } || uiState.isSaved,
                            modifier = Modifier.weight(1f).padding(start = 8.dp),
                            colors = ButtonDefaults.filledTonalButtonColors()
                        ) {
                            Text("Reset")
                        }
                    }
                }

                // Bottom Section: Navigation
                Column(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (uiState.isSaved) {
                        Button(
                            onClick = onNavigateToPublishingStudio,
                            modifier = Modifier.fillMaxWidth(0.8f),
                            colors = ButtonDefaults.buttonColors()
                        ) {
                            Text("Go to Publishing Studio")
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = onBack,
                            modifier = Modifier.weight(1f).padding(end = 4.dp),
                            colors = ButtonDefaults.filledTonalButtonColors()
                        ) {
                            Text("Go Back")
                        }
                        Button(
                            onClick = onHome,
                            modifier = Modifier.weight(1f).padding(start = 4.dp),
                            colors = ButtonDefaults.filledTonalButtonColors()
                        ) {
                            Text("Go Home")
                        }
                    }
                }
            }
        }
    }
}
