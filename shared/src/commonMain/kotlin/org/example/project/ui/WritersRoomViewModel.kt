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

    init {
        viewModelScope.launch {
            scriptDao.getActiveScript().collect { activeScript ->
                if (activeScript != null) {
                    _uiState.value = _uiState.value.copy(
                        prompt = activeScript.title,
                        generatedScript = activeScript.content,
                        durationSeconds = activeScript.targetDuration,
                        isSaved = true,
                        savedScriptId = activeScript.id
                    )
                }
            }
        }
    }

    fun updatePrompt(newPrompt: String) {
        _uiState.value = _uiState.value.copy(prompt = newPrompt)
    }

    fun updateDuration(seconds: Int) {
        _uiState.value = _uiState.value.copy(durationSeconds = seconds)
    }

    fun updateScriptContent(newContent: String) {
        _uiState.value = _uiState.value.copy(generatedScript = newContent, isSaved = false)
    }

    fun generateScript() {
        val currentPrompt = _uiState.value.prompt
        if (currentPrompt.isBlank()) return

        val hiddenFormattingRequirements = """
            
            CRITICAL FORMATTING INSTRUCTIONS:
            You are generating text strictly for a teleprompter. 
            1. You MUST NOT include ANY visual cues, camera directions, markdown, asterisks, brackets, or stage directions (e.g., [Visual: ...], (Smiling), *Cut to*).
            2. The ONLY text allowed is the literal spoken dialogue that the user will read out loud.
            3. You MUST break the text down into 5-second or 10-second segments.
            4. Every single line MUST begin with the time block in the exact format: 'Xs-Ys: '.
            5. The script MUST be designed to be read in EXACTLY ${_uiState.value.durationSeconds} seconds.
            
            Good Example:
            0s-5s: Hello everyone, today we are learning about YouTube shorts.
            5s-10s: The most important part is the hook in the first three seconds.
            
            Bad Example (DO NOT DO THIS):
            0s-5s: [Visual: Host waves] Hello everyone.
        """.trimIndent()

        val fullPrompt = currentPrompt + "\n" + hiddenFormattingRequirements

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        
        viewModelScope.launch {
            val result = geminiClient.generateScript(fullPrompt)
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
        val currentPrompt = _uiState.value.prompt
        val currentScriptId = _uiState.value.savedScriptId
        
        if (currentScriptText.isBlank()) return

        viewModelScope.launch {
            if (currentScriptId != null) {
                // Update existing script
                val existingScript = scriptDao.getScriptById(currentScriptId)
                if (existingScript != null) {
                    val updatedScript = existingScript.copy(
                        title = currentPrompt.takeIf { it.isNotBlank() } ?: "Untitled Script",
                        content = currentScriptText,
                        targetDuration = _uiState.value.durationSeconds
                    )
                    scriptDao.updateScript(updatedScript)
                    _uiState.value = _uiState.value.copy(isSaved = true)
                }
            } else {
                // Insert new script
                val script = Script(
                    title = currentPrompt.takeIf { it.isNotBlank() } ?: "Untitled Script",
                    content = currentScriptText,
                    createdAt = Clock.System.now().toEpochMilliseconds(),
                    targetDuration = _uiState.value.durationSeconds
                )
                val id = scriptDao.insertScript(script.copy(isActive = true))
                scriptDao.clearActiveScript()
                scriptDao.setActiveScript(id)
                _uiState.value = _uiState.value.copy(isSaved = true, savedScriptId = id)
            }
        }
    }

    fun advanceToRecordingStage() {
        val scriptId = _uiState.value.savedScriptId ?: return
        viewModelScope.launch {
            scriptDao.updateScriptStage(scriptId, org.example.project.domain.model.ScriptStage.RECORDING_STUDIO)
        }
    }
}

data class WritersRoomUiState(
    val prompt: String = "",
    val durationSeconds: Int = 60,
    val generatedScript: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSaved: Boolean = false,
    val savedScriptId: Long? = null
)
