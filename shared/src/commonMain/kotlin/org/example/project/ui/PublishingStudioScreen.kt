package org.example.project.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun PublishingStudioScreen(
    viewModel: PublishingStudioViewModel,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onFinish: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Card(
            modifier = Modifier.widthIn(max = 600.dp).fillMaxHeight(0.9f).padding(16.dp).offset(y = 64.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
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
                    Text("Publishing Studio", style = MaterialTheme.typography.headlineMedium)
                    
                    if (uiState.videoPath != null) {
                        Box(modifier = Modifier.fillMaxWidth().height(300.dp).background(Color.Black)) {
                            VideoPlayer(
                                modifier = Modifier.fillMaxSize(),
                                url = uiState.videoPath!!
                            )
                        }
                    } else {
                        Text("No video available to publish.", color = MaterialTheme.colorScheme.error)
                    }

                    if (uiState.publishStatus != null) {
                        Text(
                            text = uiState.publishStatus!!,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Bottom Section: Actions
                Column(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = { viewModel.publishToYouTube() },
                        enabled = uiState.videoPath != null && !uiState.isPublishing,
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        if (uiState.isPublishing) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("Save + Open YouTube")
                    }
                    Text("Click the button above, then click + icon in YouTube, then click Add (bottom left), and select your video. ")

                    Button(
                        onClick = { 
                            viewModel.finishPublishing()
                            onFinish() 
                        },
                        modifier = Modifier.fillMaxWidth(0.8f),
                        colors = ButtonDefaults.filledTonalButtonColors()
                    ) {
                        Text("Finish & Go Home")
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
