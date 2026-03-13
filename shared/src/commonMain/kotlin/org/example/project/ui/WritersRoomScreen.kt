package org.example.project.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun WritersRoomScreen(
    viewModel: WritersRoomViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier.widthIn(max = 600.dp).padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Writer's Room", style = MaterialTheme.typography.headlineMedium)

                OutlinedTextField(
                    value = uiState.prompt,
                    onValueChange = { viewModel.updatePrompt(it) },
                    label = { Text("Prompt") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Button(
                    onClick = { viewModel.generateScript() },
                    enabled = !uiState.isLoading && uiState.prompt.isNotBlank()
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

                    Button(
                        onClick = { viewModel.saveScript() },
                        enabled = uiState.generatedScript.isNotBlank()
                    ) {
                        Text(if (uiState.isSaved) "Saved!" else "Save Script")
                    }
                }

                Button(onClick = onBack) { Text("Go Home") }
            }
        }
    }
}
