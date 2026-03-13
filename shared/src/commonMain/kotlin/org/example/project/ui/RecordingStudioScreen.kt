package org.example.project.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohamedrejeb.calf.permissions.ExperimentalPermissionsApi
import com.mohamedrejeb.calf.permissions.Permission
import com.mohamedrejeb.calf.permissions.PermissionStatus
import com.mohamedrejeb.calf.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RecordingStudioScreen(
    viewModel: RecordingStudioViewModel,
    onBack: () -> Unit,
    onNavigateToEditingStudio: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val cameraPermissionState = rememberPermissionState(Permission.Camera)
    val micPermissionState = rememberPermissionState(Permission.RecordAudio)

    LaunchedEffect(Unit) {
        if (cameraPermissionState.status != PermissionStatus.Granted) cameraPermissionState.launchPermissionRequest()
        if (micPermissionState.status != PermissionStatus.Granted) micPermissionState.launchPermissionRequest()
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (cameraPermissionState.status == PermissionStatus.Granted) {
            CameraPreview(modifier = Modifier.fillMaxSize().clickable(enabled = false, onClick = {}))
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // Top Half: Teleprompter
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.errorMessage != null) {
                    Text(uiState.errorMessage!!, color = Color.Red, fontSize = 20.sp)
                } else if (uiState.countdown > 0) {
                    Text(
                        uiState.countdown.toString(),
                        color = Color.White,
                        fontSize = 80.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else if (uiState.isRecording || uiState.recordingFinished) {
                    TeleprompterText(
                        words = uiState.words,
                        currentWordIndex = uiState.currentWordIndex
                    )
                } else {
                    Text("Ready to Record", color = Color.White, fontSize = 24.sp)
                }
            }

            // Bottom Half: Controls
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (uiState.recordingFinished) {
                        Button(onClick = { viewModel.resetRecording() }) {
                            Text("Re-record")
                        }
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = onNavigateToEditingStudio) {
                            Text("Go to Editing Studio")
                        }
                    } else if (!uiState.isRecording && uiState.countdown == 0) {
                        Button(onClick = { viewModel.startRecording() }) {
                            Text("Start Recording")
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onBack) {
                        Text("Go Back")
                    }
                }
            }
        }
    }
}

@Composable
fun TeleprompterText(words: List<String>, currentWordIndex: Int) {
    val listState = rememberLazyListState()

    LaunchedEffect(currentWordIndex) {
        if (currentWordIndex > 0 && currentWordIndex < words.size) {
            listState.animateScrollToItem(currentWordIndex)
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(vertical = 100.dp)
    ) {
        itemsIndexed(words) { index, word ->
            val color = if (index < currentWordIndex) Color.Gray else if (index == currentWordIndex) Color.White else Color.LightGray
            val size = if (index == currentWordIndex) 32.sp else 24.sp
            Text(
                text = word,
                color = color,
                fontSize = size,
                fontWeight = if (index == currentWordIndex) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}
