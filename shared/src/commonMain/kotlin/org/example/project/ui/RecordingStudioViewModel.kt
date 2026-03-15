package org.example.project.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.example.project.data.local.ScriptDao
import org.example.project.domain.model.Script

class RecordingStudioViewModel(
    private val scriptDao: ScriptDao,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RecordingStudioUiState())
    val uiState: StateFlow<RecordingStudioUiState> = _uiState.asStateFlow()

    private var teleprompterJob: Job? = null

    private var targetDuration = 60

    init {
        viewModelScope.launch {
            scriptDao.getActiveScript().collect {
                if (it != null) {
                    targetDuration = it.targetDuration
                    val blocks = parseTimeBlocks(it.content)
                    _uiState.value = _uiState.value.copy(
                        scriptContent = it.content,
                        timeBlocks = blocks,
                        savedVideoPath = it.videoPath,
                        errorMessage = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(errorMessage = "No active script found.")
                }
            }
        }
    }

    private fun parseTimeBlocks(scriptContent: String): List<TeleprompterBlock> {
        val blocks = mutableListOf<TeleprompterBlock>()
        // Match things like "0s-5s: Hello world", "2s- 10s : Hi" or even "10s: Text"
        val regex = Regex("(\\d+)s(?:\\s*-\\s*(\\d+)s)?\\s*:\\s*(.*)")
        scriptContent.lines().forEach { line ->
            val match = regex.find(line.trim())
            if (match != null) {
                val start = match.groupValues[1].toInt()
                val end = match.groupValues[2].takeIf { it.isNotEmpty() }?.toInt() ?: (start + 5)
                val text = match.groupValues[3]
                blocks.add(TeleprompterBlock(start, end, text))
            } else if (line.isNotBlank() && !line.startsWith("**") && !line.startsWith("---")) {
                // Fallback for lines without timestamp
                val start = blocks.lastOrNull()?.endTime ?: 0
                blocks.add(TeleprompterBlock(start, start + 5, line.trim()))
            }
        }
        return blocks
    }

    fun startRecording() {
        if (_uiState.value.isRecording || _uiState.value.countdown > 0) return

        _uiState.value = _uiState.value.copy(countdown = 5, isRecording = false, elapsedTime = 0, savedVideoPath = null, videoError = null)
        
        viewModelScope.launch {
            while (_uiState.value.countdown > 0) {
                delay(1000)
                _uiState.value = _uiState.value.copy(countdown = _uiState.value.countdown - 1)
            }
            // Countdown finished, start recording
            _uiState.value = _uiState.value.copy(isRecording = true)
            startTeleprompter()
        }
    }

    private fun startTeleprompter() {
        teleprompterJob?.cancel()
        teleprompterJob = viewModelScope.launch {
            val totalDuration = targetDuration
            
            while (_uiState.value.elapsedTime < totalDuration) {
                delay(1000)
                _uiState.value = _uiState.value.copy(
                    elapsedTime = _uiState.value.elapsedTime + 1
                )
            }
            
            // Finished
            stopRecording()
        }
    }

    fun stopRecording() {
        teleprompterJob?.cancel()
        _uiState.value = _uiState.value.copy(isRecording = false, recordingFinished = true)
    }
    
    fun resetRecording() {
        teleprompterJob?.cancel()
        _uiState.value = _uiState.value.copy(
            isRecording = false,
            recordingFinished = false,
            countdown = 0,
            elapsedTime = 0,
            savedVideoPath = null,
            videoError = null
        )
    }

    fun advanceToEditingStage() {
        viewModelScope.launch {
            val activeScript = scriptDao.getActiveScript().firstOrNull()
            if (activeScript != null) {
                scriptDao.updateScriptStage(activeScript.id, org.example.project.domain.model.ScriptStage.EDITING_STUDIO)
            }
        }
    }
    
    fun setVideoResult(path: String?, error: String?) {
        _uiState.value = _uiState.value.copy(
            savedVideoPath = path,
            videoError = error
        )
        if (path != null) {
            viewModelScope.launch {
                val activeScript = scriptDao.getActiveScript().firstOrNull()
                if (activeScript != null) {
                    scriptDao.updateScriptVideoPath(activeScript.id, path)
                }
            }
        }
    }
}

data class TeleprompterBlock(
    val startTime: Int,
    val endTime: Int,
    val text: String
)

data class RecordingStudioUiState(
    val scriptContent: String = "",
    val timeBlocks: List<TeleprompterBlock> = emptyList(),
    val errorMessage: String? = null,
    val countdown: Int = 0,
    val isRecording: Boolean = false,
    val elapsedTime: Int = 0,
    val recordingFinished: Boolean = false,
    val savedVideoPath: String? = null,
    val videoError: String? = null
)
