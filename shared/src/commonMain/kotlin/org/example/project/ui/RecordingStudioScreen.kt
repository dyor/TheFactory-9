package org.example.project.ui


import androidx.compose.foundation.layout.*
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
import com.kashif.cameraK.compose.rememberCameraKState
import com.kashif.cameraK.compose.CameraKScreen
import com.kashif.cameraK.state.CameraConfiguration
import com.kashif.cameraK.enums.CameraLens
import com.kashif.cameraK.enums.Directory
import com.kashif.videorecorderplugin.rememberVideoRecorderPlugin
import com.kashif.cameraK.state.CameraKEvent
import com.kashif.cameraK.video.VideoConfiguration
import com.kashif.cameraK.video.VideoCaptureResult
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RecordingStudioScreen(
    viewModel: RecordingStudioViewModel,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onNavigateToEditingStudio: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    RecordingStudioScreenContent(
        uiState = uiState,
        onStartRecording = { viewModel.startRecording() },
        onResetRecording = { viewModel.resetRecording() },
        onAdvanceToEditingStage = { viewModel.advanceToEditingStage() },
        onBack = onBack,
        onHome = onHome,
        onNavigateToEditingStudio = onNavigateToEditingStudio,
        onVideoResult = { path, error -> viewModel.setVideoResult(path, error) }
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RecordingStudioScreenContent(
    uiState: RecordingStudioUiState,
    onStartRecording: () -> Unit,
    onResetRecording: () -> Unit,
    onAdvanceToEditingStage: () -> Unit,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onNavigateToEditingStudio: () -> Unit,
    onVideoResult: (String?, String?) -> Unit
) {
    val cameraPermissionState = rememberPermissionState(Permission.Camera)
    val micPermissionState = rememberPermissionState(Permission.RecordAudio)
    
    val videoPlugin = rememberVideoRecorderPlugin(config = VideoConfiguration(enableAudio = false))
    val cameraState by rememberCameraKState(
        config = CameraConfiguration(cameraLens = CameraLens.FRONT, directory = Directory.DOCUMENTS),
        setupPlugins = { it.attachPlugin(videoPlugin) }
    )

    LaunchedEffect(videoPlugin) {
        videoPlugin.recordingEvents.collect { event ->
            when (event) {
                is CameraKEvent.RecordingStopped -> {
                    val result = event.result
                    if (result is VideoCaptureResult.Success) {
                        onVideoResult(result.filePath, null)
                    } else if (result is VideoCaptureResult.Error) {
                        onVideoResult(null, result.exception.message)
                    }
                }
                is CameraKEvent.RecordingFailed -> {
                    onVideoResult(null, event.exception.message)
                }
                else -> {}
            }
        }
    }

    LaunchedEffect(Unit) {
        if (cameraPermissionState.status != PermissionStatus.Granted) cameraPermissionState.launchPermissionRequest()
        if (micPermissionState.status != PermissionStatus.Granted) micPermissionState.launchPermissionRequest()
    }

    LaunchedEffect(uiState.isRecording, uiState.recordingFinished) {
        if (uiState.isRecording) {
            videoPlugin.startRecording()
        } else if (uiState.recordingFinished) {
            videoPlugin.stopRecording()
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Card(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxHeight(0.9f)
                .padding(top = 64.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent, // Made transparent to ensure camera view is visible
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (cameraPermissionState.status == PermissionStatus.Granted) {
                    CameraKScreen(
                        modifier = Modifier.fillMaxSize(),
                        cameraState = cameraState,
                        showPreview = true
                    ) { readyState ->
                        // The camera is ready! Here we overlay the UI elements
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Top Half: Teleprompter
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .padding(top = 32.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                if (uiState.errorMessage != null) {
                                    Text(uiState.errorMessage, color = Color.Red, fontSize = 20.sp)
                                } else if (uiState.countdown > 0) {
                                    Text(
                                        uiState.countdown.toString(),
                                        color = Color.White,
                                        fontSize = 80.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else if (uiState.isRecording || uiState.recordingFinished) {
                                    TeleprompterText(
                                        timeBlocks = uiState.timeBlocks,
                                        elapsedTime = uiState.elapsedTime
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
                                        if (uiState.videoError != null) {
                                            Text(
                                                "Video Recording Failed:\n${uiState.videoError}", 
                                                color = Color.Red, 
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(bottom = 8.dp)
                                            )
                                        }

                                        Button(
                                            onClick = onResetRecording,
                                            modifier = Modifier.fillMaxWidth(0.8f),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                        ) {
                                            Text("Re-record")
                                        }
                                        Spacer(Modifier.height(16.dp))
                                        Button(
                                            onClick = { 
                                                onAdvanceToEditingStage()
                                                onNavigateToEditingStudio() 
                                            },
                                            modifier = Modifier.fillMaxWidth(0.8f)
                                        ) {
                                            Text("Go to Editing Studio")
                                        }
                                    } else if (!uiState.isRecording && uiState.countdown == 0) {
                                        Button(onClick = {
                                            onStartRecording()
                                        }) {
                                            Text("Start Recording")
                                        }
                                    }
                                    
                                    Spacer(Modifier.height(16.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(0.8f),
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
            }
        }
    }
}

@Composable
fun TeleprompterText(timeBlocks: List<TeleprompterBlock>, elapsedTime: Int) {
    // Find the block that should be currently displayed based on elapsed time.
    // If we are between blocks or just finished, find the most recent or current one.
    val activeBlock = timeBlocks.find { elapsedTime >= it.startTime && elapsedTime < it.endTime }
        ?: timeBlocks.lastOrNull { elapsedTime >= it.endTime }

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        if (activeBlock != null) {
            Text(
                text = "${activeBlock.startTime}s - ${activeBlock.endTime}s",
                color = Color.Gray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Light,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = activeBlock.text,
                color = Color.White,
                fontSize = 20.sp, // Decreased font size for readability and moved to top
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth() // Uses full width
            )
        } else {
             Text(
                text = "Get ready...",
                color = Color.Gray,
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview
@Composable
fun RecordingStudioScreenPreview() {
    MaterialTheme {
        RecordingStudioScreenContent(
            uiState = RecordingStudioUiState(
                timeBlocks = listOf(
                    TeleprompterBlock(0, 5, "Hello World!"),
                    TeleprompterBlock(5, 10, "Welcome to the recording studio.")
                ),
                countdown = 0,
                isRecording = true,
                elapsedTime = 2,
                recordingFinished = false,
                errorMessage = null
            ),
            onStartRecording = {},
            onResetRecording = {},
            onAdvanceToEditingStage = {},
            onBack = {},
            onHome = {},
            onNavigateToEditingStudio = {},
            onVideoResult = { _, _ -> }
        )
    }
}
