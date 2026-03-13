package org.example.project.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.data.local.ScriptDao
import org.example.project.domain.model.Script

class RecordingStudioViewModel(
    private val scriptDao: ScriptDao,
    private val scriptId: Long?
) : ViewModel() {
    private val _uiState = MutableStateFlow(RecordingStudioUiState())
    val uiState: StateFlow<RecordingStudioUiState> = _uiState.asStateFlow()

    private var teleprompterJob: Job? = null

    init {
        if (scriptId != null) {
            viewModelScope.launch {
                val script = scriptDao.getScriptById(scriptId)
                if (script != null) {
                    val words = script.content.split("\\s+".toRegex()).filter { it.isNotBlank() }
                    _uiState.value = _uiState.value.copy(
                        scriptContent = script.content,
                        words = words
                    )
                } else {
                    _uiState.value = _uiState.value.copy(errorMessage = "Script not found.")
                }
            }
        } else {
            _uiState.value = _uiState.value.copy(errorMessage = "No script provided.")
        }
    }

    fun startRecording() {
        if (_uiState.value.isRecording || _uiState.value.countdown > 0) return

        _uiState.value = _uiState.value.copy(countdown = 5, isRecording = false, currentWordIndex = 0)
        
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
            val totalWords = _uiState.value.words.size
            if (totalWords == 0) return@launch
            
            // We want to finish the script in 60 seconds.
            // So we delay (60,000 / totalWords) ms per word.
            val delayPerWord = 60000L / totalWords
            
            while (_uiState.value.currentWordIndex < totalWords) {
                delay(delayPerWord)
                _uiState.value = _uiState.value.copy(
                    currentWordIndex = _uiState.value.currentWordIndex + 1
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
            currentWordIndex = 0
        )
    }
}

data class RecordingStudioUiState(
    val scriptContent: String = "",
    val words: List<String> = emptyList(),
    val errorMessage: String? = null,
    val countdown: Int = 0,
    val isRecording: Boolean = false,
    val currentWordIndex: Int = 0,
    val recordingFinished: Boolean = false
)
