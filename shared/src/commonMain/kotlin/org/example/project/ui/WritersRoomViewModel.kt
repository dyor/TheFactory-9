package org.example.project.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.data.local.ScriptDao
import org.example.project.data.remote.GeminiClient
import org.example.project.domain.model.Script
import kotlin.time.Clock

class WritersRoomViewModel(
    private val scriptDao: ScriptDao,
) : ViewModel() {
    private val geminiClient = GeminiClient()

    private val _uiState = MutableStateFlow(WritersRoomUiState())
    val uiState: StateFlow<WritersRoomUiState> = _uiState.asStateFlow()

    fun updatePrompt(newPrompt: String) {
        _uiState.value = _uiState.value.copy(prompt = newPrompt)
    }

    fun updateScriptContent(newContent: String) {
        _uiState.value = _uiState.value.copy(generatedScript = newContent)
    }

    fun generateScript() {
        val currentPrompt = _uiState.value.prompt
        if (currentPrompt.isBlank()) return

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        
        viewModelScope.launch {
            val result = geminiClient.generateScript(currentPrompt)
            if (result.startsWith("Error:") || result.startsWith("Network Error:")) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    generatedScript = result,
                    errorMessage = null
                )
            }
        }
    }

    fun saveScript() {
        val currentScriptText = _uiState.value.generatedScript
        if (currentScriptText.isBlank()) return

        viewModelScope.launch {
            val script = Script(
                title = "Script " + Clock.System.now().epochSeconds.toString(),
                content = currentScriptText,
                createdAt = Clock.System.now().toEpochMilliseconds()
            )
            scriptDao.insertScript(script)
            _uiState.value = _uiState.value.copy(isSaved = true)
        }
    }
}

data class WritersRoomUiState(
    val prompt: String = "Write a script for YouTube short that is designed to teach people how to create compelling YouTube shorts.",
    val generatedScript: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSaved: Boolean = false
)
