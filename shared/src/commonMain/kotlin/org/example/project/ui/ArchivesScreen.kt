package org.example.project.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun ArchivesScreen(
    viewModel: ArchivesViewModel,
    onBack: () -> Unit
) {
    val scripts by viewModel.allScripts.collectAsState()

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
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Archives", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(16.dp))

                if (scripts.isEmpty()) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text("No scripts found.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(scripts) { script ->
                            var isExpanded by remember { mutableStateOf(false) }
                            
                            Card(
                                modifier = Modifier.fillMaxWidth().clickable { isExpanded = !isExpanded },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (script.isActive) 
                                        MaterialTheme.colorScheme.primaryContainer 
                                    else 
                                        MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (script.isActive)
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                                    val dateStr = Instant.fromEpochMilliseconds(script.createdAt)
                                        .toLocalDateTime(TimeZone.currentSystemDefault())
                                        .date.toString()
                                        
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = script.currentStage.toString().substringAfterLast("."),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = dateStr,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    if (isExpanded) {
                                        Text(
                                            text = "Prompt: ${script.title}",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = script.content,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        if (!script.isActive) {
                                            Button(
                                                onClick = { viewModel.makeScriptActive(script.id) },
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text("Make Active")
                                            }
                                        } else {
                                            Text("Currently Active", color = MaterialTheme.colorScheme.primary)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Button(
                                                onClick = { viewModel.archiveScript(script.id) },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                            ) {
                                                Text("Archive / Remove Active Status")
                                            }
                                        }
                                    } else {
                                        val previewText = script.content.split("\\s+".toRegex()).take(10).joinToString(" ") + "..."
                                        Text(
                                            text = previewText,
                                            style = MaterialTheme.typography.bodyMedium,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
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
