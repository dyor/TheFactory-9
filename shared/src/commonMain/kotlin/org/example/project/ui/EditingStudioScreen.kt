package org.example.project.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    
    val listState = rememberLazyListState()
    
    LaunchedEffect(uiState.currentPlaybackSecond) {
        val index = uiState.segments.indexOfFirst { it.second == uiState.currentPlaybackSecond }
        if (index >= 0) {
            listState.animateScrollToItem(index)
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Card(
            modifier = Modifier.widthIn(max = 600.dp).fillMaxHeight(0.9f).padding(16.dp).offset(y = 64.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Box(modifier = Modifier.fillMaxSize()) { // Add Box wrapper for absolute positioning of modal
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top section
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Editing Studio", style = MaterialTheme.typography.headlineMedium)

                        // Video Player
                        if (uiState.videoPath != null) {
                            Box(modifier = Modifier.fillMaxWidth().height(240.dp).background(Color.Black)) {
                                VideoPlayer(
                                    modifier = Modifier.fillMaxSize(),
                                    url = uiState.videoPath!!,
                                    seekRequest = seekRequest,
                                    isPlaying = uiState.isPlaying,
                                    onSeekHandled = { viewModel.clearSeekRequest() },
                                    onTimeUpdate = { viewModel.onTimeUpdate(it) },
                                    onCompletion = { viewModel.onVideoCompletion() }
                                )
                                
                                val playingSegment = uiState.segments.find { it.second == uiState.currentPlaybackSecond }
                                val isCurrentTenthSkipped = playingSegment?.skippedTenths?.contains(uiState.currentPlaybackTenth) == true
                                if (isCurrentTenthSkipped && !uiState.isPreviewMode) {
                                    Box(
                                        modifier = Modifier.fillMaxSize().background(Color.Red.copy(alpha = 0.4f)),
                                        contentAlignment = Alignment.Center
                                    ) {}
                                }
                            }
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                Button(
                                    onClick = { viewModel.togglePreviewMode() },
                                    colors = if (uiState.isPreviewMode) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error) else ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    Text(
                                        if (uiState.isPreviewMode) "Stop Previewing" else "Preview without Skipped Frames",
                                        color = if (uiState.isPreviewMode) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }

                        Box(modifier = Modifier.fillMaxWidth()) {
                            Column {
                                // Timeline simulation
                                LazyRow(
                                    state = listState,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(60.dp)
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp)
                                ) {
                                    items(uiState.segments) { segment ->
                                        val isPlaying = segment.second == uiState.currentPlaybackSecond
                                        val backgroundColor = when {
                                            segment.isFullySkipped -> MaterialTheme.colorScheme.errorContainer
                                            segment.isPartiallySkipped -> Color(0xFFFFA726) // Orange
                                            else -> MaterialTheme.colorScheme.primaryContainer
                                        }

                                        val textColor = when {
                                            segment.isFullySkipped -> MaterialTheme.colorScheme.onErrorContainer
                                            segment.isPartiallySkipped -> Color.Black
                                            else -> MaterialTheme.colorScheme.onPrimaryContainer
                                        }

                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .width(60.dp)
                                                .clip(MaterialTheme.shapes.medium)
                                                .background(backgroundColor)
                                                .then(if (isPlaying) Modifier.border(3.dp, Color(0xFF9C27B0), MaterialTheme.shapes.medium) else Modifier)
                                                .clickable { viewModel.openSegmentDetail(segment.second) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = "${segment.second}s",
                                                    color = textColor,
                                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                                )
                                                if (segment.isFullySkipped) {
                                                    Spacer(Modifier.height(4.dp))
                                                    Text("Skip", color = Color.Red, fontSize = 10.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold)
                                                } else if (segment.isPartiallySkipped) {
                                                    Spacer(Modifier.height(4.dp))
                                                    Text("Partial", color = Color.Black, fontSize = 10.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold)
                                                }
                                            }
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Button(
                                        onClick = { viewModel.saveModifications() },
                                        enabled = uiState.segments.any { !it.isNotSkipped } && !uiState.isSaving,
                                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                                        colors = if (uiState.isSaved) ButtonDefaults.filledTonalButtonColors().copy(
                                                     containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                                     contentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                                                 ) 
                                                 else if (!uiState.segments.any { !it.isNotSkipped }) ButtonDefaults.filledTonalButtonColors().copy(
                                                     disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                                     disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                                                 )
                                                 else ButtonDefaults.buttonColors()
                                    ) {
                                        if (uiState.isSaving) {
                                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                                        } else {
                                            Text(if (uiState.isSaved) "Saved!" else "Save Edits")
                                        }
                                    }

                                    Button(
                                        onClick = { viewModel.restoreOriginal() },
                                        enabled = uiState.segments.any { !it.isNotSkipped } || uiState.isSaved,
                                        modifier = Modifier.weight(1f).padding(start = 8.dp),
                                        colors = ButtonDefaults.filledTonalButtonColors()
                                    ) {
                                        Text("Reset")
                                    }
                                }
                            }
                        }
                    } // <-- This closes the Top section Column!
                    
                    // Bottom Section: Navigation
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onNavigateToPublishingStudio,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors()
                        ) {
                            Text("Go to Publishing Studio")
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
                } // <-- Closes the main vertical layout Column
                
                // Floating modal over everything
                if (uiState.selectedSegmentForDetail != null) {
                    val selectedSec = uiState.selectedSegmentForDetail!!
                    val selectedSeg = uiState.segments.find { it.second == selectedSec }
                    if (selectedSeg != null) {
                        Card(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .padding(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            elevation = CardDefaults.cardElevation(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "Fine-tune Second $selectedSec", 
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, 
                                    modifier = Modifier.padding(bottom = 12.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    (0..4).forEach { tenth ->
                                        val isSkipped = selectedSeg.skippedTenths.contains(tenth)
                                        val color = if (isSkipped) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                        val textColor = if (isSkipped) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .clip(MaterialTheme.shapes.small)
                                                .background(color)
                                                .clickable { viewModel.toggleTenthSkipped(selectedSec, tenth) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(".${tenth}s", color = textColor, fontSize = 12.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                        }
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    (5..9).forEach { tenth ->
                                        val isSkipped = selectedSeg.skippedTenths.contains(tenth)
                                        val color = if (isSkipped) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                        val textColor = if (isSkipped) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .clip(MaterialTheme.shapes.small)
                                                .background(color)
                                                .clickable { viewModel.toggleTenthSkipped(selectedSec, tenth) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(".${tenth}s", color = textColor, fontSize = 12.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                        }
                                    }
                                }
                                
                                Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Button(
                                        onClick = { viewModel.skipAllTenths(selectedSec) },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                    ) { Text("Skip All") }
                                    
                                    Button(
                                        onClick = { viewModel.showAllTenths(selectedSec) },
                                        colors = ButtonDefaults.buttonColors()
                                    ) { Text("Skip None") }
                                    
                                    IconButton(
                                        onClick = { viewModel.closeSegmentDetail() }
                                    ) { 
                                        Text("ⓧ", fontSize = 24.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) 
                                    }
                                }
                            }
                        }
                    }
                }
            } // Close the newly introduced wrapper Box
        }
    }
}
